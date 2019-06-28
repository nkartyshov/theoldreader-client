package ru.oldowl.core.extension

import android.app.AlertDialog
import ru.oldowl.R
import ru.oldowl.core.ui.BaseFragment

inline fun BaseFragment.alertDialog(
        titleRes: Int,
        messageRes: Int? = null,
        init: AlertDialog.Builder.() -> Unit
): AlertDialog.Builder = AlertDialog.Builder(this.requireContext())
        .apply {
            setTitle(titleRes)
            messageRes?.let { setMessage(it) }
        }
        .apply(init)

inline fun BaseFragment.confirmDialog(
        messageRes: Int,
        crossinline onConfirm: () -> Unit
): AlertDialog = alertDialog(R.string.warning_dialog_title, messageRes) {
            setPositiveButton(R.string.yes_dialog_button) { _,_ ->
                onConfirm()
            }

            setNegativeButton(R.string.no_dialog_button, null)
        }
        .show()