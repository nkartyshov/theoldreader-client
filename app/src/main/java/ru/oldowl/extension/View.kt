package ru.oldowl.extension

import android.support.annotation.StringRes
import android.support.design.widget.Snackbar
import android.view.View

fun Snackbar.make(view: View, @StringRes stringRes: Int, duration: Int): Snackbar {
    val context = view.context

    return Snackbar.make(view, context.getString(stringRes), duration)
}