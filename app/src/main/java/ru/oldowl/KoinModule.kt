package ru.oldowl

import org.koin.android.architecture.ext.viewModel
import org.koin.android.ext.koin.androidApplication
import org.koin.dsl.module.applicationContext
import ru.oldowl.api.TheOldReaderApi
import ru.oldowl.service.AccountService
import ru.oldowl.service.SettingsService
import ru.oldowl.viewmodel.LoginViewModel

val serviceModule = applicationContext {
    bean { TheOldReaderApi() }

    // Services
    bean { SettingsService(androidApplication()) }
    bean { AccountService(androidApplication()) }

    // ViewModels
    viewModel { LoginViewModel(androidApplication(), get(), get()) }
}