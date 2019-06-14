package ru.oldowl.repository

import android.app.job.JobInfo
import android.app.job.JobScheduler
import android.content.ComponentName
import android.content.Context
import android.os.PersistableBundle
import ru.oldowl.core.extension.isScheduled
import ru.oldowl.db.model.Subscription
import ru.oldowl.job.SyncJob
import ru.oldowl.job.SyncJob.Companion.FORCE
import ru.oldowl.job.SyncJob.Companion.SUBSCRIPTION_ID
import ru.oldowl.job.jobStatusLiveData
import java.util.concurrent.TimeUnit

private const val FORCED_UPDATE_ID = 1
private const val AUTO_UPDATE_ID = 2

class SyncManager(
        private val context: Context,
        private val settingsStorage: SettingsStorage
) {

    private val jobScheduler by lazy { context.getSystemService(Context.JOB_SCHEDULER_SERVICE) as JobScheduler }

    val state
        get() = jobStatusLiveData

    fun scheduleUpdate() {
        if (settingsStorage.autoUpdate) {
            if (!jobScheduler.isScheduled(AUTO_UPDATE_ID)) {
                val componentName = ComponentName(context, SyncJob::class.java)
                val job = JobInfo.Builder(AUTO_UPDATE_ID, componentName)
                        .setRequiredNetworkType(JobInfo.NETWORK_TYPE_ANY)
                        .setPeriodic(TimeUnit.MINUTES.toMillis(15))
                        .setPersisted(true)
                        .build()

                jobScheduler.schedule(job)
            }
        } else {
            jobScheduler.cancel(AUTO_UPDATE_ID)
        }
    }

    fun forceUpdate(subscription: Subscription?) {
        val extras = PersistableBundle()
        extras.putInt(FORCE, 1)

        subscription?.let {
            extras.putString(SUBSCRIPTION_ID, it.id)
        }

        val componentName = ComponentName(context, SyncJob::class.java)
        val job = JobInfo.Builder(FORCED_UPDATE_ID, componentName)
                .setRequiredNetworkType(JobInfo.NETWORK_TYPE_ANY)
                .setExtras(extras)
                .build()

        jobScheduler.schedule(job)
    }
}