package ru.oldowl.extension

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.support.v4.app.Fragment
import ru.oldowl.ui.BaseActivity

fun BaseActivity.openWebsite(url: String) = startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url)))

fun BaseActivity.replaceFragment(id: Int, fragment: Fragment, addToBackStack: Boolean = true) {
    val beginTransaction = supportFragmentManager.beginTransaction()
    beginTransaction.replace(id, fragment)

    if (addToBackStack) {
        beginTransaction.addToBackStack(null)
    }

    beginTransaction.commit()
}

fun Context.openUrl(url: String?) {
    url?.let {
        val intent = Intent(Intent.ACTION_VIEW).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
            data = Uri.parse(url)
        }
        startActivity(intent)
    }
}

fun Context.copyToClipboard(url: String) {
    val clipboardManager = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager?
    clipboardManager?.primaryClip = ClipData.newRawUri(url, Uri.parse(url))
}
