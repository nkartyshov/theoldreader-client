package ru.oldowl

import org.koin.android.architecture.ext.viewModel
import org.koin.android.ext.koin.androidApplication
import org.koin.dsl.module.applicationContext
import ru.oldowl.api.TheOldReaderApi
import ru.oldowl.service.AccountService
import ru.oldowl.service.SettingsService
import ru.oldowl.viewmodel.ArticleListViewModel
import ru.oldowl.viewmodel.LoginViewModel
import ru.oldowl.viewmodel.MainViewModel

val serviceModule = applicationContext {
    // Database
    bean { AppDatabase.buildDatabase(androidApplication().applicationContext) }
    bean { get<AppDatabase>().subscriptionDao() }
    bean { get<AppDatabase>().categoryDao() }
    bean { get<AppDatabase>().articleDao() }

    // API
    bean { TheOldReaderApi() }

    // Services
    bean { SettingsService(androidApplication()) }
    bean { AccountService(androidApplication()) }

    // ViewModels
    viewModel { LoginViewModel(androidApplication(), get(), get(), get(), get()) }
    viewModel { MainViewModel(get(), get(), get()) }
    viewModel { ArticleListViewModel(get(), get()) }
}