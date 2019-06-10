package ru.oldowl.job

import android.app.job.JobParameters
import android.app.job.JobService
import android.arch.lifecycle.MutableLiveData
import kotlinx.coroutines.*
import org.koin.standalone.KoinComponent
import kotlin.coroutines.CoroutineContext

val jobStatusLiveData: MutableLiveData<JobStatus> = MutableLiveData()

sealed class JobStatus {
    object Success : JobStatus()
    object InProgress : JobStatus()

    data class Failure(private val throwable: Throwable) : JobStatus()
}

abstract class CoroutineJobService : JobService(), CoroutineScope, KoinComponent {
    private val job: Job = Job()

    override val coroutineContext: CoroutineContext
        get() = Dispatchers.Main + job

    abstract suspend fun startJob(params: JobParameters?): JobStatus

    override fun onStartJob(params: JobParameters?): Boolean {
        launch {
            jobStatusLiveData.value = JobStatus.InProgress

            val jobStatus = async(Dispatchers.Default) { startJob(params) }

            jobStatusLiveData.value = jobStatus.await()
            jobFinished(params, false)
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