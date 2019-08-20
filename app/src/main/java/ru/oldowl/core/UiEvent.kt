package ru.oldowl.core

import android.view.View
import androidx.annotation.StringRes
import com.google.android.material.snackbar.Snackbar

sealed class UiEvent {

    object CloseScreen : UiEvent()
    object RefreshScreen : UiEvent()

    class ShowSnackbar(
            @StringRes val message: Int,
            val duration: Int,
            val args: MutableList<Any> = mutableListOf(),
            var action: Pair<Int, View.OnClickListener>? = null): UiEvent() {

        fun args(value: Any) = args.add(value)

        fun action(message: Int, listener: () -> Unit) {
            action = message to View.OnClickListener { listener() }
        }
    }
}