package ru.oldowl.core

import android.support.annotation.StringRes
import android.view.View
import java.lang.Exception

sealed class Event

data class ShowSnackbar(@StringRes val message: Int, val duration: Int) : Event() {
    private var actions: Array<out Pair<Int, View.OnClickListener>> = emptyArray()

    constructor(@StringRes message: Int, duration: Int, vararg actions: Pair<Int, View.OnClickListener>) : this(message, duration) {
        this.actions = actions
    }
}

data class Failure(val message: Int, val exception: Exception) : Event()