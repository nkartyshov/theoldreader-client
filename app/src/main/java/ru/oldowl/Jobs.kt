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
import org.jetbrains.anko.AnkoLogger
import org.jetbrains.anko.debug
import org.koin.standalone.KoinComponent
import org.koin.standalone.inject
import ru.oldowl.core.extension.isScheduled
import ru.oldowl.db.model.Subscription
import ru.oldowl.repository.*
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

class AutoUpdateJob : AnkoLogger, JobService(), KoinComponent, CoroutineScope {
    private val job: Job = Job()

    override val coroutineContext: CoroutineContext
        get() = Dispatchers.Default + job

    private val settingsStorage: SettingsStorage by inject()

    private val syncEventRepository: SyncEventRepository by inject()
    private val categoryRepository: CategoryRepository by inject()
    private val subscriptionRepository: SubscriptionRepository by inject()
    private val articleRepository: ArticleRepository by inject()

    override fun onStartJob(params: JobParameters?): Boolean {
        launch {
            try {
                sendJobStatus(params?.jobId, false)

                val extras = params?.extras
                val subscriptionId = extras?.getLong(SUBSCRIPTION_ID) ?: -1
                val force: Boolean = extras?.getInt(FORCE) == 1

                if (!shouldUpdateSubscription(force, settingsStorage.lastSyncDate)) {
                    debug("Autoupdate the subscriptions has been skip, last update date ${settingsStorage.lastSyncDate}")
                    return@launch
                }

                // Sync events
                syncEventRepository.syncEvents()

                // Save new categories
                categoryRepository.downloadCategory().forEach { categoryRepository.saveOrUpdate(it) }

                // Sync subscriptions
                syncSubscriptions()

                // Downloading new articles
                val subscriptions: List<Subscription> = if (subscriptionId > 0)
                    listOf(subscriptionRepository.findById(subscriptionId))
                else subscriptionRepository.findAll()

                subscriptions
                        .flatMap { articleRepository.downloadArticles(it) }
                        .forEach { articleRepository.save(it) }

                // Sync favorites
                syncFavorites()

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

    private suspend fun syncSubscriptions() {
        val old = subscriptionRepository.findAll()
        val new = subscriptionRepository.downloadSubscription()

        // Delete subscriptions
        old.minus(new).forEach { subscriptionRepository.delete(it) }

        // Save and update subscriptions
        new.forEach {
            subscriptionRepository.saveOrUpdate(it)
        }
    }

    private suspend fun syncFavorites() {
        val old = articleRepository.findFavorite().map { it.article }
        val new = articleRepository.downloadFavorites()

        // Delete favorites
        old.minus(new).forEach {
            it.favorite = false
            articleRepository.updateState(it)
        }

        new.forEach {
            articleRepository.save(it)
        }
    }
}