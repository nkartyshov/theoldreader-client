package ru.oldowl.core

import android.support.annotation.StringRes
import android.support.design.widget.Snackbar
import android.view.View

sealed class Event

object CloseScreen : Event()
object RefreshScreen: Event()

data class Failure(@StringRes val message: Int, val exception: Throwable) : Event()

data class ShowSnackbar(@StringRes val message: Int, val duration: Int) : Event() {
    var actions: Array<out Pair<Int, View.OnClickListener>> = emptyArray()

    constructor(
            @StringRes message: Int,
            duration: Int,
            vararg actions: Pair<Int, View.OnClickListener>
    ) : this(message, duration) {
        this.actions = actions
    }
}