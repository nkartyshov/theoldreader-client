package ru.oldowl

import android.app.Application
import android.preference.PreferenceManager
import android.webkit.WebView
import org.koin.android.ext.android.startKoin

@Suppress("unused")
class AppApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        startKoin(this, listOf(serviceModule))

        PreferenceManager.setDefaultValues(this, R.xml.settings, true)

        Jobs.scheduleUpdate(applicationContext)

        WebView.setWebContentsDebuggingEnabled(BuildConfig.DEBUG)
    }
}