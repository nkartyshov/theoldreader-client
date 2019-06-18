package ru.oldowl.core.extension

import android.app.Activity
import android.app.job.JobScheduler
import android.arch.lifecycle.LifecycleOwner
import android.arch.lifecycle.LiveData
import android.arch.lifecycle.Observer
import android.content.*
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.support.annotation.StringRes
import android.support.customtabs.CustomTabsIntent
import android.support.v4.app.Fragment
import android.support.v4.content.ContextCompat
import android.support.v7.preference.Preference
import android.support.v7.preference.PreferenceFragmentCompat
import android.view.inputmethod.InputMethodManager
import ru.oldowl.R
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
        val customTabsIntent = CustomTabsIntent.Builder()
                .setShowTitle(true)
                .setToolbarColor(ContextCompat.getColor(this, R.color.colorPrimary))
                .setInstantAppsEnabled(false)
                .addDefaultShareMenuItem()
                .build()

        with(customTabsIntent) {
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
            launchUrl(this@browse, Uri.parse(url))
        }
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

inline fun <reified T : Context> Context.startActivity(bundle: Bundle? = null) {
    val intent = Intent(this, T::class.java)

    bundle?.let {
        intent.putExtras(it)
    }

    this.startActivity(intent)
}

fun PreferenceFragmentCompat.findPreference(@StringRes strignRes: Int): Preference {
    val key = this.context?.getString(strignRes)
    return this.findPreference(key)
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

fun <T : Any> LifecycleOwner.observe(liveData: LiveData<T>, body: (T?) -> Unit) {
    liveData.observe(this, Observer(body))
}
