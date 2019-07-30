package ru.oldowl.core

import androidx.annotation.StringRes
import com.google.android.material.snackbar.Snackbar

sealed class UiEvent {

    object CloseScreen : UiEvent()
    object RefreshScreen : UiEvent()

    class ShowSnackbar(
            @StringRes val message: Int,
            val args: List<Any> = emptyList(),
            val duration: Int = Snackbar.LENGTH_SHORT) : UiEvent()
}