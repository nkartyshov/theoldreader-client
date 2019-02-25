package ru.oldowl.viewmodel

import android.app.Application
import android.arch.lifecycle.LiveData
import android.arch.lifecycle.MediatorLiveData
import android.arch.lifecycle.MutableLiveData
import android.arch.lifecycle.Observer
import ru.oldowl.*
import ru.oldowl.dao.ArticleDao
import ru.oldowl.model.ArticleAndSubscriptionTitle
import ru.oldowl.model.Subscription

class ArticleListViewModel(private val application: Application,
                           private val articleDao: ArticleDao) : BaseViewModel() {

    private val mediatorLiveData = MediatorLiveData<List<ArticleAndSubscriptionTitle>>()

    private val unreadLiveData: LiveData<List<ArticleAndSubscriptionTitle>> by lazy {
        when (mode) {
            ArticleListMode.ALL -> articleDao.observeAllUnread()
            ArticleListMode.FAVORITE -> articleDao.observeFavorite()
            ArticleListMode.SUBSCRIPTION -> articleDao.observeUnread(subscription?.id!!)
        }
    }

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

    val title: String
        get() {
            return when (mode) {
                ArticleListMode.SUBSCRIPTION -> subscription?.title!!
                ArticleListMode.FAVORITE -> application.getString(R.string.favorite_title)
                else -> application.getString(R.string.all_title)
            }
        }

    val articles: LiveData<List<ArticleAndSubscriptionTitle>> by lazy {
        mediatorLiveData.addSource(unreadLiveData) {
            mediatorLiveData.value = it
        }

        mediatorLiveData
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
}

enum class ArticleListMode {
    ALL, FAVORITE, SUBSCRIPTION
}