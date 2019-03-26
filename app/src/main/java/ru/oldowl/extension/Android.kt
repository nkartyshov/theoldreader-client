package ru.oldowl.extension

import android.app.job.JobScheduler
import android.content.*
import android.net.Uri
import android.os.Build
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

fun Context.browse(url: String? = ""): Boolean {
    return try {
        val intent = Intent(Intent.ACTION_VIEW).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
            data = Uri.parse(url)
        }
        startActivity(intent)
        true
    } catch(e: ActivityNotFoundException) {
        e.printStackTrace()
        false
    }
}

fun Context.share(text: String? = "", subject: String = ""): Boolean {
    return try {
        val intent = Intent(android.content.Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(android.content.Intent.EXTRA_SUBJECT, subject)
            putExtra(android.content.Intent.EXTRA_TEXT, text)
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

fun JobScheduler.isScheduled(jobId: Int): Boolean {
    val job = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
        this.getPendingJob(jobId)
    } else {
        this.allPendingJobs.singleOrNull { it.id == jobId }
    }

    return job != null
}
