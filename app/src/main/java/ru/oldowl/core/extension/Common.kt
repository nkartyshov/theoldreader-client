package ru.oldowl.core.extension

import com.google.android.material.snackbar.Snackbar
import android.view.View
import ru.oldowl.core.UiEvent.*
import java.text.DateFormat
import java.util.*

val Date.epochTime
    get() = this.time / 1000

fun Date?.toEpochTime(): String? =
        this?.epochTime?.toString()

fun Date.toShortDateTime(): String =
        DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.SHORT).format(this)

fun showMessage(view: View, event: ShowSnackbar) {
    val context = view.context
    val message = context.getString(event.message, *event.args.toTypedArray())

    val snackbar = Snackbar.make(view, message, event.duration)

    event.action?.let {
        snackbar.setAction(it.first, it.second)
    }

    snackbar.show()
}