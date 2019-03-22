package ru.oldowl

import android.app.job.JobInfo
import android.app.job.JobParameters
import android.app.job.JobScheduler
import android.app.job.JobService
import android.arch.lifecycle.LiveData
import android.arch.lifecycle.MutableLiveData
import android.content.ComponentName
import android.content.Context
import android.os.PersistableBundle
import kotlinx.coroutines.*
import org.koin.standalone.KoinComponent
import org.koin.standalone.inject
import ru.oldowl.api.TheOldReaderApi
import ru.oldowl.dao.ArticleDao
import ru.oldowl.dao.CategoryDao
import ru.oldowl.dao.EventDao
import ru.oldowl.dao.SubscriptionDao
import ru.oldowl.extension.afterOrEquals
import ru.oldowl.extension.exists
import ru.oldowl.model.Article
import ru.oldowl.model.Category
import ru.oldowl.model.EventType
import ru.oldowl.model.Subscription
import ru.oldowl.service.AccountService
import ru.oldowl.service.SettingsService
import java.util.*
import kotlin.coroutines.CoroutineContext

const val FORCED_UPDATE_ID = 1
const val AUTO_UPDATE_ID = 2

private const val SUBSCRIPTION_ID = "subscription_id"
private const val FORCE = "force"

private val observeJobStatus: MutableLiveData<JobStatus> = MutableLiveData()

data class JobStatus(val jobId: Int?, val finished: Boolean)

object Jobs {
    val observeJobStatus: LiveData<JobStatus> = ru.oldowl.observeJobStatus

    fun forceUpdate(context: Context) {
        Jobs.forceUpdate(context, null)
    }

    fun forceUpdate(context: Context, subscription: Subscription?) {
        val jobScheduler = context.getSystemService(Context.JOB_SCHEDULER_SERVICE) as JobScheduler

        val intent = PersistableBundle()
        intent.putInt(FORCE, 1)

        subscription?.let {
            intent.putLong(SUBSCRIPTION_ID, it.id)
        }

        val componentName = ComponentName(context, AutoUpdateJob::class.java)
        val job = JobInfo.Builder(FORCED_UPDATE_ID, componentName)
                .setRequiredNetworkType(JobInfo.NETWORK_TYPE_ANY)
                .setExtras(PersistableBundle())
                .build()

        jobScheduler.schedule(job)
    }
}

private suspend fun sendJobStatus(jobId: Int?, finished: Boolean) = withContext(Dispatchers.Main) {
    observeJobStatus.value = JobStatus(jobId, finished)
}

class AutoUpdateJob : JobService(), KoinComponent, CoroutineScope {
    private val job: Job = Job()

    override val coroutineContext: CoroutineContext
        get() = Dispatchers.Default + job

    private val accountService: AccountService by inject()
    private val settingsService: SettingsService by inject()

    private val theOldReaderApi: TheOldReaderApi by inject()

    private val subscriptionDao: SubscriptionDao by inject()
    private val categoryDao: CategoryDao by inject()
    private val articleDao: ArticleDao by inject()
    private val eventDao: EventDao by inject()

    override fun onStartJob(params: JobParameters?): Boolean {
        launch {
            // TODO update auth token

            val extras = params?.extras

            val subscriptionId = extras?.getLong(SUBSCRIPTION_ID) ?: -1
            val forced: Boolean = extras?.getInt(FORCE) == 1

            //FIXME Add update period
            val lastSyncDate = settingsService.lastSyncDate

            if (!forced && lastSyncDate.afterOrEquals(Date())) {
                return@launch
            }

            try {
                sendJobStatus(params?.jobId, false)

                val account = accountService.getAccount()
                val authToken = account?.authToken ?: ""

                // Sync events
                synchronization(authToken)

                // Sync subscriptions
                syncSubscriptions(authToken)

                // Sync favorites
                syncFavorites(authToken)

                // Downloading new articles
                val subscriptions: List<Subscription> = if (subscriptionId > 0)
                    listOf(subscriptionDao.findById(subscriptionId))
                else subscriptionDao.findAll()

                for (subscription in subscriptions) {
                    val feedId = subscription.feedId ?: ""

                    val itemIds = theOldReaderApi.getItemIds(feedId, authToken, newerThan = lastSyncDate)
                    if (itemIds.isNotEmpty()) {
                        val contents = theOldReaderApi.getContents(itemIds, authToken)

                        if (contents.isNotEmpty()) {
                            val articles = contents.map {
                                Article(
                                        originalId = it.itemId,
                                        title = it.title,
                                        description = it.description,
                                        subscriptionId = subscription.id,
                                        url = it.link,
                                        publishDate = it.publishDate
                                )
                            }

                            articles.forEach {
                                if (!articleDao.exists(it.originalId)) {
                                    articleDao.save(it)
                                }
                            }
                        }
                    }
                }

                settingsService.lastSyncDate = Date()
            } finally {
                sendJobStatus(params?.jobId, true)
                jobFinished(params, false)
            }
        }

        return true
    }

    override fun onStopJob(params: JobParameters?): Boolean {
        if (job.isActive) {
            job.cancel()
        }

        jobFinished(params, false)

        return true
    }

    private fun synchronization(authToken: String) {
        val events = eventDao.findAll()
        for (event in events) {
            when (event.eventType) {
                EventType.UPDATE_READ -> event.payload?.let {
                    val article = articleDao.findByOriginalId(it)
                    theOldReaderApi.updateReadState(article!!.originalId, article.read, authToken)
                }

                EventType.UPDATE_FAVORITE -> event.payload?.let {
                    val article = articleDao.findByOriginalId(it)
                    theOldReaderApi.updateFavoriteState(article!!.originalId, article.favorite, authToken)
                }

                EventType.MARK_ALL_READ -> {
                    theOldReaderApi.markAllRead(event.payload, authToken, event.createdDate)
                }

                EventType.UNSUBSCRIBE -> event.payload?.let {
                    theOldReaderApi.unsubscribe(it, authToken)
                }
            }
        }
    }

    private fun syncFavorites(authToken: String) {
        val favoriteIds = theOldReaderApi.getFavoriteIds(authToken)
        val favorites = articleDao.findFavorite()

        /*if (favorites.isEmpty()) {
            val contents = theOldReaderApi.getContents(favoriteIds, authToken)

            val articles = contents.map {
                Article(
                        originalId = it.itemId,
                        title = it.title,
                        description = it.description,
                        subscriptionId = subscription.id,
                        url = it.link,
                        publishDate = it.publishDate
                )
            }

            articles.forEach {
                if (!articleDao.exists(it.originalId)) {
                    articleDao.save(it)
                }
            }
        }*/

        // Delete favorite
        for (favorite in favorites) {
            if (!favoriteIds.exists { it == favorite.article.originalId }) {
                articleDao.delete(favorite.article)
            }
        }

        // Update favorite
        for (favoriteId in favoriteIds) {
            val article = articleDao.findByOriginalId(favoriteId)
            article?.let {
                articleDao.updateFavoriteState(it.id, true)
            }
        }
    }

    private fun syncSubscriptions(authToken: String) {
        val subscriptions = subscriptionDao.findAll()
        val subscriptionResponses = theOldReaderApi.getSubscriptions(authToken)

        // Delete subscriptions
        for (subscription in subscriptions) {
            if (subscriptionResponses.exists { subscription.feedId == it.id }) {
                subscriptionDao.delete(subscription)
            }
        }

        // Adds or update subscriptions
        for (subscriptionResponse in subscriptionResponses) {
            val categories = subscriptionResponse.categories
            val category = categories
                    .map { Category(labelId = it.id, title = it.label) }
                    .getOrElse(0) { Category(1, "default", "Default") }

            val categoryId = findCategoryOrSave(category)

            val subscription = Subscription(
                    categoryId = categoryId,
                    title = subscriptionResponse.title,
                    feedId = subscriptionResponse.id,
                    url = subscriptionResponse.url,
                    htmlUrl = subscriptionResponse.htmlUrl
            )

            val oldSubscription = subscriptions.singleOrNull { it.feedId == subscriptionResponse.id }
            if (oldSubscription != null) {
                subscription.id = oldSubscription.id
                subscriptionDao.update(subscription)
            } else {
                val subscriptionId = subscriptionDao.save(subscription)
                subscription.id = subscriptionId
            }
        }
    }

    private fun findCategoryOrSave(category: Category): Long {
        return if (categoryDao.exists(category.labelId))
            categoryDao.findIdByLabelId(category.labelId)
        else categoryDao.save(category)
    }
}