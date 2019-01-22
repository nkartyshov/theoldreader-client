package ru.oldowl

import org.koin.android.ext.koin.androidApplication
import org.koin.dsl.module.applicationContext
import ru.oldowl.api.TheOldReaderApi
import ru.oldowl.service.SettingsService

val serviceModule = applicationContext {
    bean { TheOldReaderApi() }
    bean { SettingsService(androidApplication()) }
}