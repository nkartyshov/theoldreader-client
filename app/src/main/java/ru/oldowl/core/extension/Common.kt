package ru.oldowl.core.extension

import android.support.design.widget.Snackbar
import android.view.View
import ru.oldowl.core.UiEvent.*
import java.text.DateFormat
import java.util.*

val Date.epochTime
    get() = this.time / 1000

fun Date.toShortDateTime() =
        DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.SHORT).format(this)

fun showMessage(view: View, event: ShowSnackbar) {
    val context = view.context
    val message = context.getString(event.message, *event.args.toTypedArray())
    Snackbar.make(view, message, event.duration).show()
}