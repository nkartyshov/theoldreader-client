package ru.oldowl

import android.app.Application
import org.koin.android.ext.android.startKoin

@Suppress("unused")
class AppApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        startKoin(this, listOf(serviceModule))
    }
}