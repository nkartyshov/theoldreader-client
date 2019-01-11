package ru.oldowl

import org.koin.android.ext.koin.androidApplication
import org.koin.dsl.module.applicationContext
import ru.oldowl.service.SettingsService
import ru.oldowl.service.TheOldReaderService

val serviceModule = applicationContext {
    bean { SettingsService(androidApplication()) }
    bean { TheOldReaderService(androidApplication()) }
}