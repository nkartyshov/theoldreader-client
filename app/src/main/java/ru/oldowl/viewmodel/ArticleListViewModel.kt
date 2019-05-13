package ru.oldowl.viewmodel

import android.app.Application
import android.arch.lifecycle.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import ru.oldowl.*
import ru.oldowl.R
import ru.oldowl.api.theoldreader.TheOldReaderApi
import ru.oldowl.core.onFailure
import ru.oldowl.core.onSuccess
import ru.oldowl.db.dao.ArticleDao
import ru.oldowl.db.dao.SyncEventDao
import ru.oldowl.db.dao.SubscriptionDao
import ru.oldowl.db.model.*
import ru.oldowl.service.AccountService
import ru.oldowl.service.SettingsService
import ru.oldowl.usecase.LoadArticleListUseCase
import ru.oldowl.usecase.Param

class ArticleListViewModel(private val application: Application,
                           private val theOldReaderApi: TheOldReaderApi,
                           private val accountService: AccountService,
                           private val articleDao: ArticleDao,
                           private val subscriptionDao: SubscriptionDao,
                           private val syncEventDao: SyncEventDao,
                           private val settingsService: SettingsService,

                           private val loadArticleListUseCase: LoadArticleListUseCase) : BaseViewModel(), LifecycleObserver {

    private val account by lazy { accountService.getAccount() }

    private val articleLiveData: MutableLiveData<List<ArticleAndSubscriptionTitle>> = MutableLiveData()

    private val jobStatusObserver: Observer<JobStatus?> by lazy {
        Observer<JobStatus?> {
            when (it?.jobId) {
                FORCED_UPDATE_ID, AUTO_UPDATE_ID -> {
                    dataLoading.value = !it.finished
                    loadArticles()
                }
            }
        }
    }

    val dataLoading = MutableLiveData<Boolean>()
    val unsubscribe = MutableLiveData<Unit>()

    var mode: ArticleListMode = ArticleListMode.ALL
    var subscription: Subscription? = null

    var hideRead: Boolean
        get() {
            return settingsService.hideRead
        }
        set(value) {
            if (settingsService.hideRead != value) {
                settingsService.hideRead = value

                loadArticles()
            }
        }

    val title: String
        get() {
            return when (mode) {
                ArticleListMode.SUBSCRIPTION -> subscription?.title!!
                ArticleListMode.FAVORITE -> application.getString(R.string.favorite_title)
                else -> application.getString(R.string.all_title)
            }
        }

    val articles: LiveData<List<ArticleAndSubscriptionTitle>>
        get() {
            return articleLiveData
        }

    init {
        Jobs.observeJobStatus.observeForever(jobStatusObserver)
    }

    fun sync() {
        if (mode == ArticleListMode.ALL)
            Jobs.forceUpdate(application)
        else if (mode == ArticleListMode.SUBSCRIPTION)
            Jobs.forceUpdate(application, subscription)
    }

    fun deleteAll() = launch {
        when (mode) {
            ArticleListMode.SUBSCRIPTION -> articleDao.deleteAll(subscription?.id)
            else -> articleDao.deleteAll()
        }

        loadArticles()
    }

    fun deleteAllRead() = launch {
        when (mode) {
            ArticleListMode.SUBSCRIPTION -> articleDao.deleteAllRead(subscription?.id)
            else -> articleDao.deleteAllRead()
        }

        loadArticles()
    }

    fun markReadAll() = launch {
        when (mode) {
            ArticleListMode.SUBSCRIPTION -> articleDao.markAllRead(subscription?.id)
            else -> articleDao.markAllRead()
        }

        syncEventDao.save(SyncEvent(eventType = SyncEventType.MARK_ALL_READ, payload = subscription?.feedId))
        loadArticles()
    }

    fun unsubscribe() = launch {
        subscription?.let {
            if (theOldReaderApi.unsubscribe(it.feedId!!, account?.authToken!!)) {
                subscriptionDao.delete(it)
                syncEventDao.save(SyncEvent(eventType = SyncEventType.UNSUBSCRIBE, payload = it.feedId))

                launch(Dispatchers.Main) {
                    unsubscribe.value = Unit
                }
            }
        }
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_RESUME)
    fun loadArticles() {

        val param = Param(hideRead, mode, subscription?.id)
        loadArticleListUseCase(param) {
            it.onSuccess {articles ->
                launch(Dispatchers.Main) {
                    articleLiveData.value = articles
                }
            }

            it.onFailure {
                // TODO show error snackbar
            }
        }
    }

    override fun onCleared() {
        super.onCleared()

        Jobs.observeJobStatus.removeObserver(jobStatusObserver)
    }
}

enum class ArticleListMode {
    ALL, FAVORITE, SUBSCRIPTION
}