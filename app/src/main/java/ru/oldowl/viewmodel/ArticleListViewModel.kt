package ru.oldowl.viewmodel

import android.app.Application
import android.arch.lifecycle.LiveData
import android.arch.lifecycle.MediatorLiveData
import android.arch.lifecycle.MutableLiveData
import android.arch.lifecycle.Observer
import kotlinx.coroutines.launch
import ru.oldowl.*
import ru.oldowl.api.TheOldReaderApi
import ru.oldowl.dao.ArticleDao
import ru.oldowl.model.ArticleAndSubscriptionTitle
import ru.oldowl.model.Subscription
import ru.oldowl.service.SettingsService

class ArticleListViewModel(private val application: Application,
                           private val articleDao: ArticleDao,
                           private val theOldReaderApi: TheOldReaderApi,
                           private val settingsService: SettingsService) : BaseViewModel() {

    private lateinit var articleLiveData: LiveData<List<ArticleAndSubscriptionTitle>>

    private val mediatorLiveData: MediatorLiveData<List<ArticleAndSubscriptionTitle>> = MediatorLiveData()

    private val jobStatusObserver: Observer<JobStatus?> by lazy {
        Observer<JobStatus?> {
            when (it?.jobId) {
                FORCED_UPDATE_ID, AUTO_UPDATE_ID -> dataLoading.value = !it.finished
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

                reloadLiveData()
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
            reloadLiveData()
            return mediatorLiveData
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

    fun deleteAllRead() {
        articleDao.deleteAllRead()
    }

    fun markReadAll() = launch {
        // TODO add event for mark all read
        articleDao.markAllRead()
    }

    fun unsubscribe() {
        TODO("not implemented")

        // TODO add event for unsubscribe
        // TODO sending unsubscribe request when network available
    }

    private fun reloadLiveData() {
        articleLiveData = when (mode) {
            ArticleListMode.FAVORITE -> articleDao.observeFavorite()

            ArticleListMode.ALL -> {
                if (hideRead)
                    articleDao.observeUnread()
                else
                    articleDao.observeAll()
            }

            ArticleListMode.SUBSCRIPTION -> {
                if (hideRead)
                    articleDao.observeUnread(subscription?.id)
                else
                    articleDao.observeAll(subscription?.id)
            }
        }

        mediatorLiveData.removeSource(articleLiveData)
        mediatorLiveData.addSource(articleLiveData) {
            mediatorLiveData.value = it
        }
    }
}

enum class ArticleListMode {
    ALL, FAVORITE, SUBSCRIPTION
}