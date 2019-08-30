package ru.oldowl.viewmodel

import android.app.Application
import android.os.Bundle
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import ru.oldowl.R
import ru.oldowl.core.ui.BaseViewModel
import ru.oldowl.db.model.ArticleListItem
import ru.oldowl.db.model.Subscription
import ru.oldowl.job.JobStatus
import ru.oldowl.repository.SettingsStorage
import ru.oldowl.repository.SyncManager
import ru.oldowl.usecase.*

class ArticleListViewModel(
        private val application: Application,
        private val settingsStorage: SettingsStorage,
        private val syncManager: SyncManager,
        private val loadArticleListUseCase: LoadArticleListUseCase,
        private val deleteAllUseCase: DeleteAllUseCase,
        private val deleteAllReadUseCase: DeleteAllReadUseCase,
        private val markReadAllUseCase: MarkAllReadUseCase,
        private val unsubscribeUseCase: UnsubscribeUseCase,
        private val addSubscriptionUseCase: AddSubscriptionUseCase,
        private val markUnreadUseCase: MarkAllUnreadUseCase,
        private val addArticlesUseCase: AddArticlesUseCase
) : BaseViewModel() {

    private val articleLiveData: MutableLiveData<List<ArticleListItem>> = MutableLiveData()

    private val jobStatusObserver by lazy {
        Observer<JobStatus?> {
            when (it) {
                is JobStatus.Success -> {
                    dataLoading.value = false
                    loadArticles()
                }

                is JobStatus.Failure -> {
                    dataLoading.value = false
                    showOopsSnackBar()
                }

                is JobStatus.InProgress -> dataLoading.value = true
            }
        }
    }

    private var mode: ArticleListMode = ArticleListMode.ALL
    private var subscription: Subscription? = null

    val dataLoading = MutableLiveData<Boolean>()

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
                onSuccess {
                    it?.let { list ->
                        if (list.isNotEmpty()) {
                            loadArticles()
                            undoDeleteArticles(list)
                        }
                    }
                }
                onFailure {
                    showOopsSnackBar()
                }
            }

    fun deleteAllRead() =
            deleteAllReadUseCase(subscription?.id) {
                onSuccess {
                    it?.let { list ->
                        if (list.isNotEmpty()) {
                            loadArticles()
                            undoDeleteArticles(list)
                        }
                    }
                }
                onFailure {
                    showOopsSnackBar()
                }
            }

    fun markReadAll() =
            markReadAllUseCase(subscription) {
                onSuccess {
                    it?.let { list ->
                        if (list.isNotEmpty()) {
                            loadArticles()
                            undoMarkAllRead(list)
                        }
                    }
                }
                onFailure {
                    showOopsSnackBar()
                }
            }

    fun unsubscribe() =
            subscription?.let { s ->
                unsubscribeUseCase(s) {
                    onSuccess {
                        addSubscription(s)
                    }

                    onFailure {
                        showOopsSnackBar()
                    }
                }
            }


    override fun onCleared() {
        super.onCleared()
        syncManager.state.removeObserver(jobStatusObserver)
    }

    fun loadArticles(query: String? = null) {
        val param = LoadArticleListUseCase.Param(mode, hideRead, subscription?.id, query)
        loadArticleListUseCase(param) {
            onSuccess { articles ->
                articleLiveData.value = articles
            }

            onFailure {
                showOopsSnackBar()
            }
        }
    }

    private fun undoMarkAllRead(ids: List<String>) =
            showLongSnackbar(R.string.mark_all_read_snackbar) {
                args(ids.size)

                action(R.string.undo) {
                    markUnreadUseCase(ids) {
                        onSuccess {
                            loadArticles()
                        }

                        onFailure {
                            showOopsSnackBar()
                        }
                    }
                }
            }

    private fun undoDeleteArticles(ids: List<String>) =
            showLongSnackbar(R.string.delete_articles_snackbar) {
                args(ids.size)

                action(R.string.undo) {
                    addArticlesUseCase(ids) {
                        onSuccess {
                            loadArticles()
                        }

                        onFailure {
                            showOopsSnackBar()
                        }
                    }
                }
            }

    private fun addSubscription(subscription: Subscription) =
            showShortSnackbar(R.string.unsubscribe_snackbar) {
                args(subscription.title)

                action(R.string.undo) {
                    addSubscriptionUseCase(subscription) {

                        onFailure {
                            showOopsSnackBar()
                        }
                    }
                }
            }

    companion object {
        const val ARTICLE_LIST_MODE = "article_list_mode"
        const val SUBSCRIPTION = "subscription"
    }
}

enum class ArticleListMode {
    ALL, FAVORITE, SUBSCRIPTION
}