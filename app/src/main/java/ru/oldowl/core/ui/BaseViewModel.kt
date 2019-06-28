package ru.oldowl.core.ui

import android.arch.lifecycle.MutableLiveData
import android.arch.lifecycle.ViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import org.koin.standalone.KoinComponent
import ru.oldowl.R
import ru.oldowl.core.UiEvent
import ru.oldowl.core.UiEvent.ShowSnackbar
import kotlin.coroutines.CoroutineContext

abstract class BaseViewModel : ViewModel(), KoinComponent, CoroutineScope {
    private val job = Job()

    override val coroutineContext: CoroutineContext
        get() = job + Dispatchers.Main

    val event = MutableLiveData<UiEvent>()

    override fun onCleared() {
        super.onCleared()

        if (job.isActive) {
            job.cancel()
        }
    }

    protected fun showOopsSnackBar() {
        event.value = ShowSnackbar(R.string.something_went_wrong_error)
    }
}