package ru.oldowl.core.extension

import android.app.Activity
import android.app.job.JobScheduler
import android.arch.lifecycle.LifecycleOwner
import android.arch.lifecycle.LiveData
import android.arch.lifecycle.Observer
import android.content.*
import android.net.Uri
import android.os.Build
import android.support.annotation.StringRes
import android.support.design.widget.Snackbar
import android.support.v4.app.Fragment
import android.view.View
import android.view.inputmethod.InputMethodManager
import ru.oldowl.core.Failure
import ru.oldowl.core.ShowSnackbar
import ru.oldowl.core.ui.BaseActivity

fun BaseActivity.replaceFragment(id: Int, fragment: Fragment, addToBackStack: Boolean = true) {
    val beginTransaction = supportFragmentManager.beginTransaction()
    beginTransaction.replace(id, fragment)

    if (addToBackStack) {
        beginTransaction.addToBackStack(null)
    }

    beginTransaction.commit()
}

fun Context.browse(url: String? = ""): Boolean {
    return try {
        val intent = Intent(Intent.ACTION_VIEW).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
            data = Uri.parse(url)
        }
        startActivity(intent)
        true
    } catch (e: ActivityNotFoundException) {
        e.printStackTrace()
        false
    }
}

fun Context.share(text: String? = "", subject: String = ""): Boolean {
    return try {
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_SUBJECT, subject)
            putExtra(Intent.EXTRA_TEXT, text)
        }
        startActivity(Intent.createChooser(intent, null))
        true
    } catch (e: ActivityNotFoundException) {
        e.printStackTrace()
        false
    }
}

fun Context.copyToClipboard(url: String) {
    val clipboardManager = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager?
    clipboardManager?.primaryClip = ClipData.newRawUri(url, Uri.parse(url))
}

fun Activity.hideSoftKeyboard() {
    val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager?
    currentFocus?.let {
        imm?.hideSoftInputFromWindow(it.windowToken, 0)
    }
}

fun JobScheduler.isScheduled(jobId: Int): Boolean {
    val job = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
        this.getPendingJob(jobId)
    } else {
        this.allPendingJobs.singleOrNull { it.id == jobId }
    }

    return job != null
}

fun Snackbar.make(view: View, @StringRes stringRes: Int, duration: Int): Snackbar {
    val context = view.context

    return Snackbar.make(view, context.getString(stringRes), duration)
}

fun <T : Any> LifecycleOwner.observe(liveData: LiveData<T>, body: (T?) -> Unit) {
    liveData.observe(this, Observer(body))
}
