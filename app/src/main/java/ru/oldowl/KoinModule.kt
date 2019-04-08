package ru.oldowl

import org.koin.android.ext.koin.androidApplication
import org.koin.android.viewmodel.ext.koin.viewModel
import org.koin.dsl.module.module
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import ru.oldowl.api.feedly.FeedlyApi
import ru.oldowl.api.feedly.FeedlyWebService
import ru.oldowl.api.theoldreader.TheOldReaderApi
import ru.oldowl.db.AppDatabase
import ru.oldowl.service.AccountService
import ru.oldowl.service.SettingsService
import ru.oldowl.viewmodel.*

val serviceModule = module {
    // Database
    single { AppDatabase.buildDatabase(androidApplication().applicationContext) }
    single { get<AppDatabase>().subscriptionDao() }
    single { get<AppDatabase>().categoryDao() }
    single { get<AppDatabase>().articleDao() }
    single { get<AppDatabase>().eventDao() }

    // Retrofit
    single {
        Retrofit.Builder()
                .baseUrl(FeedlyWebService.BASE_URL)
                .addConverterFactory(MoshiConverterFactory.create())
                .build()
                .create(FeedlyWebService::class.java)
    }

    // API
    single { TheOldReaderApi() }
    single { FeedlyApi(get()) }

    // Services
    single { SettingsService(androidApplication()) }
    single { AccountService(androidApplication()) }

    // ViewModels
    viewModel { (appName: String) -> LoginViewModel(appName, get(), get()) }
    viewModel { MainViewModel(get(), get(), get()) }
    viewModel { ArticleListViewModel(get(), get(), get(), get(), get()) }
    viewModel { ArticleViewModel(get(), get()) }
    viewModel { AddSubscriptionViewModel(get()) }
}