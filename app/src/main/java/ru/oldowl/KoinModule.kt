package ru.oldowl

import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import org.koin.android.ext.koin.androidApplication
import org.koin.android.viewmodel.ext.koin.viewModel
import org.koin.dsl.module.module
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import ru.oldowl.api.feedly.FeedlyApi
import ru.oldowl.api.feedly.FeedlyWebService
import ru.oldowl.api.theoldreader.TheOldReaderApi
import ru.oldowl.api.theoldreader.TheOldReaderWebService
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

    // OkHttp
    single {
        val logger = HttpLoggingInterceptor()
        logger.level = HttpLoggingInterceptor.Level.BODY

        OkHttpClient.Builder()
                .addInterceptor(logger)
                .build()
    }

    // Moshi
    single {
        Moshi.Builder()
                .add(KotlinJsonAdapterFactory())
                .build()
    }

    // Retrofit
    single {
        Retrofit.Builder()
                .baseUrl(FeedlyWebService.BASE_URL)
                .addConverterFactory(MoshiConverterFactory.create(get()))
                .client(get())
                .build()
                .create(FeedlyWebService::class.java)
    }

    single {
        Retrofit.Builder()
                .baseUrl(TheOldReaderWebService.BASE_URL)
                .addConverterFactory(MoshiConverterFactory.create(get()).asLenient())
                .client(get())
                .build()
                .create(TheOldReaderWebService::class.java)
    }

    // API
    single { TheOldReaderApi(get(), get()) }
    single { FeedlyApi(get()) }

    // Services
    single { SettingsService(androidApplication()) }
    single { AccountService(androidApplication(), get()) }

    // ViewModels
    viewModel { (appName: String) -> LoginViewModel(appName, get(), get()) }
    viewModel { MainViewModel(get(), get(), get()) }
    viewModel { ArticleListViewModel(get(), get(), get(), get(), get()) }
    viewModel { ArticleViewModel(get(), get()) }
    viewModel { AddSubscriptionViewModel(get(), get()) }
}