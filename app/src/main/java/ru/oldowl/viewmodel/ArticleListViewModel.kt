package ru.oldowl.viewmodel

import android.app.Application
import android.arch.lifecycle.LifecycleObserver
import android.arch.lifecycle.LiveData
import android.arch.lifecycle.MutableLiveData
import android.arch.lifecycle.Observer
import android.os.Bundle
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import ru.oldowl.*
import ru.oldowl.core.CloseScreen
import ru.oldowl.core.Event
import ru.oldowl.core.ui.BaseViewModel
import ru.oldowl.db.model.ArticleAndSubscriptionTitle
import ru.oldowl.db.model.Subscription
import ru.oldowl.service.SettingsService
import ru.oldowl.usecase.*

class ArticleListViewModel(private val application: Application,
                           private val settingsService: SettingsService,
                           private val loadArticleListUseCase: LoadArticleListUseCase,
                           private val deleteAllUseCase: DeleteAllUseCase,
                           private val deleteAllReadUseCase: DeleteAllReadUseCase,
                           private val markReadAllUseCase: MarkAllReadUseCase,
                           private val unsubscribeUseCase: UnsubscribeUseCase) : BaseViewModel(), LifecycleObserver {

    private val articleLiveData: MutableLiveData<List<ArticleAndSubscriptionTitle>> = MutableLiveData()

    @Deprecated(message = "")
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

    private var mode: ArticleListMode = ArticleListMode.ALL
    private var subscription: Subscription? = null

    val dataLoading = MutableLiveData<Boolean>()
    val event = MutableLiveData<Event>()

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

    fun setArgument(arguments: Bundle?) = arguments?.let {
        mode = it.getSerializable(ARTICLE_LIST_MODE) as ArticleListMode
        subscription = it.getParcelable(SUBSCRIPTION) as Subscription?
    }

    fun isFavoriteMode() = mode == ArticleListMode.FAVORITE

    fun hasSubscription() = subscription != null

    fun sync() {
        if (mode == ArticleListMode.ALL)
            Jobs.forceUpdate(application)
        else if (mode == ArticleListMode.SUBSCRIPTION)
            Jobs.forceUpdate(application, subscription)
    }

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

        val param = LoadArticleListUseCase.Param(hideRead, mode, subscription?.id)
        loadArticleListUseCase(param) {
            onSuccess { articles ->
                launch(Dispatchers.Main) {
                    articleLiveData.value = articles
                }
            }

            onFailure {
                // TODO show error snackbar
            }
        }
    }

    override fun onCleared() {
        super.onCleared()

        Jobs.observeJobStatus.removeObserver(jobStatusObserver)
    }

    companion object {
        const val ARTICLE_LIST_MODE = "article_list_mode"
        const val SUBSCRIPTION = "subscription"
    }
}

enum class ArticleListMode {
    ALL, FAVORITE, SUBSCRIPTION
}