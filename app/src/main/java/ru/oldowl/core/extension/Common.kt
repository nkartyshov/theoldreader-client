package ru.oldowl.core.extension

import android.support.design.widget.Snackbar
import android.view.View
import ru.oldowl.core.Failure
import ru.oldowl.core.ShowSnackbar
import java.text.DateFormat
import java.util.*

val Date.epochTime
    get() = this.time / 1000

fun Date.toShortDateTime() =
        DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.SHORT).format(this)

inline fun <T> Iterable<T>.exists(predicate: (T) -> Boolean): Boolean {
    for (item in this) {
        if (predicate.invoke(item)) {
            return true
        }
    }

    return false
}

fun showMessage(view: View, event: ShowSnackbar) {
    Snackbar.make(view, event.message, event.duration).apply {
       /* event.actions.forEach {
            setAction(it.first, it.second)
        }*/
    }.show()
}

fun showFailure(view: View, event: Failure) {
    Snackbar.make(view, event.message, Snackbar.LENGTH_SHORT).show()
}