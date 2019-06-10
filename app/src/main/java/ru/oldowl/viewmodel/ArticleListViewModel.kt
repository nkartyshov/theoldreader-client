package ru.oldowl.viewmodel

import android.app.Application
import android.arch.lifecycle.LifecycleObserver
import android.arch.lifecycle.LiveData
import android.arch.lifecycle.MutableLiveData
import android.arch.lifecycle.Observer
import android.os.Bundle
import ru.oldowl.R
import ru.oldowl.core.CloseScreen
import ru.oldowl.core.Event
import ru.oldowl.core.ui.BaseViewModel
import ru.oldowl.db.model.ArticleListItem
import ru.oldowl.db.model.Subscription
import ru.oldowl.job.JobStatus
import ru.oldowl.repository.SettingsStorage
import ru.oldowl.repository.SyncManager
import ru.oldowl.usecase.*

class ArticleListViewModel(private val application: Application,
                           private val settingsStorage: SettingsStorage,
                           private val syncManager: SyncManager,
                           private val loadArticleListUseCase: LoadArticleListUseCase,
                           private val deleteAllUseCase: DeleteAllUseCase,
                           private val deleteAllReadUseCase: DeleteAllReadUseCase,
                           private val markReadAllUseCase: MarkAllReadUseCase,
                           private val unsubscribeUseCase: UnsubscribeUseCase) : BaseViewModel(), LifecycleObserver {

    private val articleLiveData: MutableLiveData<List<ArticleListItem>> = MutableLiveData()

    private val jobStatusObserver: Observer<JobStatus?> by lazy {
        Observer<JobStatus?> {
            when (it) {
                is JobStatus.Success -> {
                    dataLoading.value = false
                    loadArticles()
                }

                is JobStatus.Failure -> {
                    dataLoading.value = false
                    // TODO show error
                }

                is JobStatus.InProgress -> dataLoading.value = true
            }
        }
    }

    private var mode: ArticleListMode = ArticleListMode.ALL
    private var subscription: Subscription? = null

    val dataLoading = MutableLiveData<Boolean>()
    val event = MutableLiveData<Event>()

    var hideRead: Boolean
        get() {
            return settingsStorage.hideRead
        }
        set(value) {
            if (settingsStorage.hideRead != value) {
                settingsStorage.hideRead = value

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

    val articles: LiveData<List<ArticleListItem>>
        get() {
            return articleLiveData
        }

    init {
        syncManager.state.observeForever(jobStatusObserver)
    }

    fun setArgument(arguments: Bundle?) = arguments?.let {
        mode = it.getSerializable(ARTICLE_LIST_MODE) as ArticleListMode
        subscription = it.getParcelable(SUBSCRIPTION) as Subscription?
    }

    fun isFavoriteMode() = mode == ArticleListMode.FAVORITE

    fun hasSubscription() = subscription != null

    fun sync() = syncManager.forceUpdate(subscription)

    fun deleteAll() =
            deleteAllUseCase(subscription?.id) {
                onSuccess { loadArticles() }
                onFailure {
                    // TODO show error snackbar
                }
            }

    fun deleteAllRead() =
            deleteAllReadUseCase(subscription?.id) {
                onSuccess { loadArticles() }
                onFailure {
                    // TODO show error snackbar
                }
            }

    fun markReadAll() =
            markReadAllUseCase(subscription) {
                onSuccess { loadArticles() }
                onFailure {
                    // TODO show error snackbar
                }
            }

    fun unsubscribe() =
            subscription?.let {
                unsubscribeUseCase(it) {
                    onSuccess { event.value = CloseScreen }
                    onFailure {
                        // TODO show error snackbar
                    }
                }
            }


    fun loadArticles() {
        val param = LoadArticleListUseCase.Param(mode, subscription?.id)
        loadArticleListUseCase(param) {
            onSuccess { articles ->
                articleLiveData.value = articles
            }

            onFailure {
                // TODO show error snackbar
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        syncManager.state.removeObserver(jobStatusObserver)
    }

    companion object {
        const val ARTICLE_LIST_MODE = "article_list_mode"
        const val SUBSCRIPTION = "subscription"
    }
}

enum class ArticleListMode {
    ALL, FAVORITE, SUBSCRIPTION
}