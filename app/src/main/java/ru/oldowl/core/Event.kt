package ru.oldowl.core

import android.support.annotation.StringRes
import android.support.design.widget.Snackbar
import android.view.View

// TODO Подумать как лучше это передалать
sealed class Event

object CloseScreen : Event()
object RefreshScreen: Event()

data class Failure(@StringRes val message: Int, val exception: Throwable) : Event()

data class ShowSnackbar(
        @StringRes val message: Int,
        val duration: Int = Snackbar.LENGTH_SHORT) : Event()

// TODO Это наверное не нужно!
@Deprecated(message = "It will replace to ShowSnackbar")
data class AddSubscriptionSuccess(val title: String): Event()