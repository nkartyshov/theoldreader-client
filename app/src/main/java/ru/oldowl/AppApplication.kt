package ru.oldowl

import android.app.Application
import android.preference.PreferenceManager
import android.webkit.WebView
import com.crashlytics.android.Crashlytics
import io.fabric.sdk.android.Fabric
import org.koin.android.ext.android.startKoin
import timber.log.Timber

@Suppress("unused")
class AppApplication : Application() {

    override fun onCreate() {
        super.onCreate()
        startKoin(this, listOf(serviceModule))

        PreferenceManager.setDefaultValues(this, R.xml.settings, true)

        WebView.setWebContentsDebuggingEnabled(BuildConfig.DEBUG)

        Timber.plant(Timber.DebugTree())

        Fabric.with(Fabric.Builder(this)
                .kits(Crashlytics())
                .debuggable(BuildConfig.DEBUG)
                .build())
    }
}