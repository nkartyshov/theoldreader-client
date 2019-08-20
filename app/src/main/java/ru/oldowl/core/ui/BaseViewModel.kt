package ru.oldowl.core.ui

import androidx.annotation.StringRes
import androidx.lifecycle.ViewModel
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import org.koin.standalone.KoinComponent
import ru.oldowl.R
import ru.oldowl.core.SingleLiveEvent
import ru.oldowl.core.UiEvent
import ru.oldowl.core.UiEvent.*
import kotlin.coroutines.CoroutineContext

abstract class BaseViewModel : ViewModel(), KoinComponent, CoroutineScope {
    private val job = Job()

    override val coroutineContext: CoroutineContext
        get() = job + Dispatchers.Main

    val event = SingleLiveEvent<UiEvent>()

    override fun onCleared() {
        super.onCleared()

        if (job.isActive) {
            job.cancel()
        }
    }

    protected fun showShortSnackbar(message: Int, block: ShowSnackbar.() -> Unit = {}) {
        event.value = ShowSnackbar(message, Snackbar.LENGTH_SHORT)
                .apply(block)
    }

    protected fun showOopsSnackBar() {
        event.value = showSnackbar(R.string.something_went_wrong_error, Snackbar.LENGTH_SHORT)
    }

    private fun showSnackbar(@StringRes message: Int, duration: Int, block: ShowSnackbar.() -> Unit = {}): ShowSnackbar
            = ShowSnackbar(message, duration)
            .apply(block)
}
