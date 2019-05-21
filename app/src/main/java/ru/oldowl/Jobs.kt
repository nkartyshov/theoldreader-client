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
import android.util.Log
import kotlinx.coroutines.*
import org.koin.standalone.KoinComponent
import org.koin.standalone.inject
import ru.oldowl.api.theoldreader.TheOldReaderApi
import ru.oldowl.repository.SettingsStorage
import ru.oldowl.db.dao.ArticleDao
import ru.oldowl.db.dao.CategoryDao
import ru.oldowl.db.dao.SyncEventDao
import ru.oldowl.db.dao.SubscriptionDao
import ru.oldowl.db.model.Article
import ru.oldowl.db.model.Category
import ru.oldowl.db.model.Subscription
import ru.oldowl.core.extension.exists
import ru.oldowl.core.extension.isScheduled
import ru.oldowl.db.model.SyncEventType
import ru.oldowl.repository.AccountRepository
import java.util.*
import java.util.concurrent.TimeUnit
import kotlin.coroutines.CoroutineContext

const val FORCED_UPDATE_ID = 1
const val AUTO_UPDATE_ID = 2

private const val SUBSCRIPTION_ID = "subscription_id"
private const val FORCE = "force"

private val observeJobStatus: MutableLiveData<JobStatus> = MutableLiveData()

data class JobStatus(val jobId: Int?, val finished: Boolean)

object Jobs : KoinComponent {
    private val SETTINGS_STORAGE: SettingsStorage by inject()

    val observeJobStatus: LiveData<JobStatus> = ru.oldowl.observeJobStatus

    fun scheduleUpdate(context: Context) {
        val jobScheduler = context.getSystemService(Context.JOB_SCHEDULER_SERVICE) as JobScheduler

        if (SETTINGS_STORAGE.autoUpdate) {
            val componentName = ComponentName(context, AutoUpdateJob::class.java)
            val job = JobInfo.Builder(AUTO_UPDATE_ID, componentName)
                    .setRequiredNetworkType(JobInfo.NETWORK_TYPE_ANY)
                    .setPeriodic(TimeUnit.MINUTES.toMillis(15))
                    .setPersisted(true)
                    .build()

            if (!jobScheduler.isScheduled(AUTO_UPDATE_ID)) {
                jobScheduler.schedule(job)
            }
        } else {
            jobScheduler.cancel(AUTO_UPDATE_ID)
        }
    }

    fun forceUpdate(context: Context) {
        forceUpdate(context, null)
    }

    fun forceUpdate(context: Context, subscription: Subscription?) {
        val jobScheduler = context.getSystemService(Context.JOB_SCHEDULER_SERVICE) as JobScheduler

        val extras = PersistableBundle()
        extras.putInt(FORCE, 1)

        subscription?.let {
            extras.putString(SUBSCRIPTION_ID, it.id)
        }

        val componentName = ComponentName(context, AutoUpdateJob::class.java)
        val job = JobInfo.Builder(FORCED_UPDATE_ID, componentName)
                .setRequiredNetworkType(JobInfo.NETWORK_TYPE_ANY)
                .setExtras(extras)
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

    private val accountRepository: AccountRepository by inject()
    private val settingsStorage: SettingsStorage by inject()

    private val theOldReaderApi: TheOldReaderApi by inject()

    private val subscriptionDao: SubscriptionDao by inject()
    private val categoryDao: CategoryDao by inject()
    private val articleDao: ArticleDao by inject()
    private val syncEventDao: SyncEventDao by inject()

    override fun onStartJob(params: JobParameters?): Boolean {
        launch {
            try {
                sendJobStatus(params?.jobId, false)
                val extras = params?.extras

                val subscriptionId = extras?.getLong(SUBSCRIPTION_ID) ?: -1
                val force: Boolean = extras?.getInt(FORCE) == 1

                val lastSyncDate = settingsStorage.lastSyncDate
                if (!shouldUpdateSubscription(force, lastSyncDate)) {
                    Log.d(AutoUpdateJob::class.simpleName, "Autoupdate the subscriptions has been skip, last update date $lastSyncDate")
                    return@launch
                }

                // TODO update auth token
                val account = accountRepository.getAccount()
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
                    val feedId = subscription.id

                    val itemIds = theOldReaderApi.getItemIds(feedId, authToken, newerThan = lastSyncDate)
                    if (itemIds.isNotEmpty()) {
                        Log.d(AutoUpdateJob::class.simpleName, "Downloading content for subscription $feedId, item size ${itemIds.size}")

                        val contents = theOldReaderApi.getContents(itemIds, authToken)
                        if (contents.isNotEmpty()) {
                            contents
                                    .map {
                                        Article(
                                                id = it.itemId,
                                                title = it.title,
                                                description = it.description,
                                                subscriptionId = subscription.id,
                                                url = it.link,
                                                publishDate = it.publishDate
                                        )
                                    }
                                    .forEach { articleDao.save(it) }
                        }
                    }
                }

                settingsStorage.lastSyncDate = Date()
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

    private fun shouldUpdateSubscription(force: Boolean, lastSyncDate: Date?): Boolean {
        val updatePeriodTime = TimeUnit.HOURS.toMillis(settingsStorage.autoUpdatePeriod)

        return force || lastSyncDate == null || (System.currentTimeMillis() - lastSyncDate.time) > updatePeriodTime
    }

    private fun synchronization(authToken: String) {
        syncEventDao.findAll()
                .forEach { event ->
                    when (event.eventType) {
                        SyncEventType.UPDATE_READ -> event.payload?.let {
                            val article = articleDao.findById(it)
                            theOldReaderApi.updateReadState(article!!.id, article.read, authToken)
                        }

                        SyncEventType.UPDATE_FAVORITE -> event.payload?.let {
                            val article = articleDao.findById(it)
                            theOldReaderApi.updateFavoriteState(article!!.id, article.favorite, authToken)
                        }

                        SyncEventType.MARK_ALL_READ -> event.payload?.let {
                            theOldReaderApi.markAllRead(it, authToken, event.createdDate)
                        }

                        SyncEventType.UNSUBSCRIBE -> event.payload?.let {
                            theOldReaderApi.unsubscribe(it, authToken)
                        }
                    }

                    syncEventDao.delete(event)
                }
    }

    private fun syncFavorites(authToken: String) {
        val favoriteIds = theOldReaderApi.getFavoriteIds(authToken)
        val favorites = articleDao.findFavorite()

        if (favorites.size != favoriteIds.size) {

            val subscriptions = subscriptionDao.findAll()
            val contents = theOldReaderApi.getContents(favoriteIds, authToken)

            // Delete favorite
            if (favorites.isNotEmpty()) {
                favorites
                        .filter { !favoriteIds.exists { id -> id == it.article.id } }
                        .forEach { articleDao.updateFavoriteState(it.article.id, false) }
            }

            contents
                    .filter { subscriptions.exists { s -> it.feedId == s.id } }
                    .map {
                        val subscription = subscriptions.single { subscription -> subscription.id == it.feedId }

                        Article(
                                id = it.itemId,
                                title = it.title,
                                description = it.description,
                                subscriptionId = subscription.id,
                                url = it.link,
                                publishDate = it.publishDate
                        )
                    }
                    .forEach {
                        articleDao.save(it)
                        articleDao.updateReadState(it.id, true)
                        articleDao.updateFavoriteState(it.id, true)
                    }
        }
    }

    private fun syncSubscriptions(authToken: String) {
        val subscriptions = subscriptionDao.findAll()
        val subscriptionResponses = theOldReaderApi.getSubscriptions(authToken)

        // Delete subscriptions
        subscriptions
                .filter { !subscriptionResponses.exists { s -> s.id == it.id } }
                .forEach { subscriptionDao.delete(it) }

        // Adds or update subscriptions
        for (subscriptionResponse in subscriptionResponses) {
            val categories = subscriptionResponse.categories
            val category = categories
                    .map { Category(id = it.id, title = it.label) }
                    .getOrElse(0) { Category("default", "Default") }

            val categoryId = findCategoryOrSave(category)
            val subscription = Subscription(
                    categoryId = categoryId,
                    id = subscriptionResponse.id,
                    title = subscriptionResponse.title,
                    url = subscriptionResponse.url,
                    htmlUrl = subscriptionResponse.htmlUrl
            )

            val oldSubscription = subscriptions.singleOrNull { it.id == subscriptionResponse.id }
            if (oldSubscription != null) {
                subscriptionDao.update(subscription)
            } else {
                subscriptionDao.save(subscription)
            }
        }
    }

    private fun findCategoryOrSave(category: Category): String {
        if (!categoryDao.exists(category.id))
            categoryDao.save(category)
        return categoryDao.findById(category.id).id
    }
}