package ru.oldowl

import org.koin.android.ext.koin.androidApplication
import org.koin.android.viewmodel.ext.koin.viewModel
import org.koin.dsl.module.module
import ru.oldowl.api.TheOldReaderApi
import ru.oldowl.service.AccountService
import ru.oldowl.service.SettingsService
import ru.oldowl.viewmodel.ArticleListViewModel
import ru.oldowl.viewmodel.ArticleViewModel
import ru.oldowl.viewmodel.LoginViewModel
import ru.oldowl.viewmodel.MainViewModel

val serviceModule = module {
    // Database
    single { AppDatabase.buildDatabase(androidApplication().applicationContext) }
    single { get<AppDatabase>().subscriptionDao() }
    single { get<AppDatabase>().categoryDao() }
    single { get<AppDatabase>().articleDao() }

    // API
    single { TheOldReaderApi() }

    // Services
    single { SettingsService(androidApplication()) }
    single { AccountService(androidApplication()) }

    // ViewModels
    viewModel { (appName : String) -> LoginViewModel(appName, get(), get()) }
    viewModel { MainViewModel(get(), get(), get()) }
    viewModel { ArticleListViewModel(get(), get(), get(), get(), get()) }
    viewModel { ArticleViewModel(get()) }
}