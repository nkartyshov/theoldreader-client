package ru.oldowl.core

import android.support.annotation.StringRes
import android.support.design.widget.Snackbar

sealed class UiEvent {

    object CloseScreen : UiEvent()
    object RefreshScreen : UiEvent()

    class ShowSnackbar(
            @StringRes val message: Int,
            val args: List<Any> = emptyList(),
            val duration: Int = Snackbar.LENGTH_SHORT) : UiEvent()
}