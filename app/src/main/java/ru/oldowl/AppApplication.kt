package ru.oldowl

import android.app.Application
import android.webkit.WebView
import org.koin.android.ext.android.startKoin

@Suppress("unused")
class AppApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        startKoin(this, listOf(serviceModule))

        Jobs.scheduleUpdate(applicationContext)

        WebView.setWebContentsDebuggingEnabled(BuildConfig.DEBUG)
    }
}