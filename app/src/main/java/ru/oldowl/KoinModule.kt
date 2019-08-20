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
import ru.oldowl.api.TheOldReaderConverterFactory
import ru.oldowl.api.feedly.FeedlyApi
import ru.oldowl.api.feedly.FeedlyWebService
import ru.oldowl.api.theoldreader.TheOldReaderApi
import ru.oldowl.api.theoldreader.TheOldReaderWebService
import ru.oldowl.db.AppDatabase
import ru.oldowl.repository.*
import ru.oldowl.usecase.*
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
                .addConverterFactory(TheOldReaderConverterFactory())
                .addConverterFactory(MoshiConverterFactory.create(get()))
                .client(get())
                .build()
                .create(TheOldReaderWebService::class.java)
    }

    // API
    single { TheOldReaderApi(get()) }
    single { FeedlyApi(get()) }

    // Repository
    single { NotificationManager(androidApplication()) }
    single { SettingsStorage(androidApplication(), get()) }
    single { SyncManager(androidApplication(), get()) }
    single<SyncEventRepository> { SyncEventRepository.SyncEventRepositoryImpl(get(), get(), get(), get()) }
    single<AccountRepository> { AccountRepository.AccountRepositoryImpl(get()) }
    single<CategoryRepository> { CategoryRepository.CategoryRepositoryImpl(get(), get(), get()) }
    single<SubscriptionRepository> { SubscriptionRepository.SubscriptionRepositoryImpl(get(), get(), get(), get()) }
    single<ArticleRepository> { ArticleRepository.ArticleRepositoryImpl(get(), get(), get(), get(), get()) }

    // Use case
    single { LoginUseCase(BuildConfig.APPLICATION_ID, get(), get()) }
    single { GetNavigationItemListUseCase(get()) }
    single { ToggleFavoriteUseCase(get()) }
    single { ToggleReadUseCase(get()) }
    single { LoadArticleListUseCase(get(), get()) }
    single { MarkAllReadUseCase(get()) }
    single { UnsubscribeUseCase(get()) }
    single { DeleteAllUseCase(get()) }
    single { DeleteAllReadUseCase(get()) }
    single { SearchSubscriptionUseCase(get()) }
    single { AddSubscriptionUseCase(get()) }
    single { MarkAllUnreadUseCase(get()) }
    single { AddArticlesUseCase(get()) }

    // ViewModels
    viewModel { LoginViewModel(get()) }
    viewModel { ArticleViewModel(get(), get()) }
    viewModel { AddSubscriptionViewModel(get(), get()) }
    viewModel { MainViewModel(get(), get(), get(), get()) }
    viewModel {
        ArticleListViewModel(get(), get(), get(), get(),
                get(), get(), get(), get(), get(), get())
    }
}