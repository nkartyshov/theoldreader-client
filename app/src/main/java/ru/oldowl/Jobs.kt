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
import ru.oldowl.dao.SubscriptionDao
import ru.oldowl.model.Article
import ru.oldowl.model.Subscription
import ru.oldowl.service.AccountService
import ru.oldowl.service.SettingsService
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
            intent.putLong(SUBSCRIPTION_ID, it.id!!)
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
    private val articleDao: ArticleDao by inject()

    override fun onStartJob(params: JobParameters?): Boolean {
        launch {
            // TODO check auth token
            // TODO check last sync date

            val extras = params?.extras

            val forced: Boolean = extras?.getInt(FORCE) == 1
            val subscriptionId = extras?.getLong(SUBSCRIPTION_ID)
            val lastSyncDate = settingsService.lastSyncDate

            try {
                sendJobStatus(params?.jobId, false)

                val account = accountService.getAccount()

                val subscriptions: List<Subscription> = if (subscriptionId != null)
                    listOf(subscriptionDao.findById(subscriptionId))
                else subscriptionDao.findAll()

                val lastItem = articleDao.findLastItem()

                for (subscription in subscriptions) {
                    val token = account?.authToken ?: ""

                    // Sync articles
                    // TODO Implement events table

                    // Downloading new articles
                    val feedId = subscription.feedId ?: ""

                    val itemIds = theOldReaderApi.getItemIds(feedId, token, true, lastItem.originalId)
                    val contents = theOldReaderApi.getContents(itemIds, token)

                    if (contents.isNotEmpty()) {
                        val articles = contents.map {
                            Article(
                                    originalId = it.itemId,
                                    title = it.title,
                                    description = it.description,
                                    subscriptionId = subscription.id
                            )
                        }

                        articles.forEach {
                            if (!articleDao.exists(it.originalId)) {
                                articleDao.save(it)
                            }
                        }
                    }
                }
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

}