package ru.oldowl.viewmodel

import android.arch.lifecycle.ViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import org.jetbrains.anko.AnkoLogger
import org.koin.standalone.KoinComponent
import kotlin.coroutines.CoroutineContext

abstract class BaseViewModel : ViewModel(), KoinComponent, CoroutineScope, AnkoLogger {
    private val job = Job()

    override val coroutineContext: CoroutineContext
        get() = job + Dispatchers.Default


    override fun onCleared() {
        super.onCleared()

        if (job.isActive) {
            job.cancel()
        }
    }
}