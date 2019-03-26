package ru.oldowl.viewmodel

import android.app.Application
import android.arch.lifecycle.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import ru.oldowl.*
import ru.oldowl.R
import ru.oldowl.db.dao.ArticleDao
import ru.oldowl.db.dao.EventDao
import ru.oldowl.db.dao.SubscriptionDao
import ru.oldowl.db.model.ArticleAndSubscriptionTitle
import ru.oldowl.db.model.Event
import ru.oldowl.db.model.EventType
import ru.oldowl.db.model.Subscription
import ru.oldowl.service.SettingsService

class ArticleListViewModel(private val application: Application,
                           private val articleDao: ArticleDao,
                           private val subscriptionDao: SubscriptionDao,
                           private val eventDao: EventDao,
                           private val settingsService: SettingsService) : BaseViewModel(), LifecycleObserver {


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

    override fun onCleared() {
        super.onCleared()

        Jobs.observeJobStatus.removeObserver(jobStatusObserver)
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

        eventDao.save(Event(eventType = EventType.MARK_ALL_READ, payload = subscription?.feedId))

        loadArticles()
    }

    fun unsubscribe() {
        subscription?.let {
            subscriptionDao.delete(it)
            eventDao.save(Event(eventType = EventType.UNSUBSCRIBE, payload = it.feedId))
        }
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_RESUME)
    fun loadArticles() {
        launch {
            val articles = when (mode) {
                ArticleListMode.FAVORITE -> articleDao.findFavorite()

                ArticleListMode.ALL -> {
                    if (hideRead)
                        articleDao.findUnread()
                    else
                        articleDao.findAll()
                }

                ArticleListMode.SUBSCRIPTION -> {
                    if (hideRead)
                        articleDao.findUnread(subscription?.id)
                    else
                        articleDao.findAll(subscription?.id)
                }
            }

            withContext(Dispatchers.Main) {
                articleLiveData.value = articles
            }
        }
    }
}

enum class ArticleListMode {
    ALL, FAVORITE, SUBSCRIPTION
}