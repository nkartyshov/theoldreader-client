package ru.oldowl

import android.app.Application
import android.webkit.WebView
import com.crashlytics.android.Crashlytics
import io.fabric.sdk.android.Fabric
import org.koin.android.ext.android.startKoin

@Suppress("unused")
class AppApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        startKoin(this, listOf(serviceModule))

        Jobs.scheduleUpdate(applicationContext)

        WebView.setWebContentsDebuggingEnabled(BuildConfig.DEBUG)

        Fabric.with(Fabric.Builder(this)
                .kits(Crashlytics())
                .debuggable(BuildConfig.DEBUG)
                .build())
    }
}