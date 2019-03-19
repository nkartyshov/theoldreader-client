package ru.oldowl.viewmodel

import android.app.Application
import android.arch.lifecycle.LiveData
import android.arch.lifecycle.MutableLiveData
import android.arch.lifecycle.Observer
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import ru.oldowl.*
import ru.oldowl.api.TheOldReaderApi
import ru.oldowl.dao.ArticleDao
import ru.oldowl.dao.SubscriptionDao
import ru.oldowl.model.ArticleAndSubscriptionTitle
import ru.oldowl.model.Subscription
import ru.oldowl.service.SettingsService

class ArticleListViewModel(private val application: Application,
                           private val articleDao: ArticleDao,
                           private val subscriptionDao: SubscriptionDao,
                           private val theOldReaderApi: TheOldReaderApi,
                           private val settingsService: SettingsService) : BaseViewModel() {


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
            loadArticles()
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
        articleDao.deleteAll()
    }

    fun deleteAllRead() = launch {
        articleDao.deleteAllRead()
    }

    fun markReadAll() = launch {
        // TODO add event for mark all read
        // TODO sending mark all read request when network available

        articleDao.markAllRead()
    }

    fun unsubscribe() {
        // TODO add event for unsubscribe
        // TODO sending unsubscribe request when network available

        subscription?.let {
            subscriptionDao.delete(it)
        }
    }

    private fun loadArticles() = launch {
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

enum class ArticleListMode {
    ALL, FAVORITE, SUBSCRIPTION
}