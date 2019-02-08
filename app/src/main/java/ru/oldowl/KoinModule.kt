package ru.oldowl

import org.koin.android.architecture.ext.viewModel
import org.koin.android.ext.koin.androidApplication
import org.koin.dsl.module.applicationContext
import ru.oldowl.api.TheOldReaderApi
import ru.oldowl.repository.SubscriptionRepository
import ru.oldowl.service.AccountService
import ru.oldowl.service.SettingsService
import ru.oldowl.viewmodel.LoginViewModel
import ru.oldowl.viewmodel.MainViewModel

val serviceModule = applicationContext {
    // Database
    bean { AppDatabase.buildDatabase(androidApplication()) }
    bean { get<AppDatabase>().subscriptionDao() }
    bean { get<AppDatabase>().categoryDao() }
    bean { get<AppDatabase>().articleDao() }

    bean { SubscriptionRepository(get()) }

    // API
    bean { TheOldReaderApi() }

    // Services
    bean { SettingsService(androidApplication()) }
    bean { AccountService(androidApplication()) }

    // ViewModels
    viewModel { LoginViewModel(androidApplication(), get(), get()) }
    viewModel { MainViewModel(get(), get(), get()) }
}