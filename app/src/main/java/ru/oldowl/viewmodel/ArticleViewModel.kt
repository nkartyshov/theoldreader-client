package ru.oldowl.viewmodel

import android.os.Bundle
import ru.oldowl.core.UiEvent.RefreshScreen
import ru.oldowl.core.ui.BaseViewModel
import ru.oldowl.db.model.ArticleListItem
import ru.oldowl.usecase.MarkReadUseCase
import ru.oldowl.usecase.ToggleFavoriteUseCase
import java.util.*

class ArticleViewModel(
        private val toggleFavoriteUseCase: ToggleFavoriteUseCase,
        private val markReadUseCase: MarkReadUseCase
) : BaseViewModel() {

    private lateinit var item: ArticleListItem

    val title: String?
        get() = item.article.title

    val subscriptionTitle: String?
        get() = item.subscriptionTitle

    val publishDate: Date?
        get() = item.article.publishDate

    val favorite: Boolean
        get() = item.article.favorite

    val url: String?
        get() = item.article.url

    fun setArgument(bundle: Bundle?) = bundle?.let {
        item = bundle.getParcelable(ARTICLE) as ArticleListItem
    }

    fun getPageContent(): String {
        // TODO Исправить ширину body
        return buildString {
            append("<html>\n")
            append("<head>\n")
            append("<meta charset=\"utf-8\" />\n")
            append("<meta name='viewport' content='width=device-width'/>\n")
            append("<style type=\"text/css\">\n")
            append("* { max-width: 100%; word-break: break-word }\n")
            append("body { padding: 0 0.2cm 0 0.2cm; }\n")
            // FIXME doesn't hide iframe
            append("iframe { display: none; }\n")
            append("img { height: auto }\n")
            append("</style>\n")
            append("</head>\n")
            append("<body>\n")
            append(item.article.description)
            append("</body>\n")
            append("</html>\n")
        }
    }

    fun toggleFavorite() {
        toggleFavoriteUseCase(item.article) {
            onSuccess { event.value = RefreshScreen }
            onFailure {
                showOopsSnackBar()
            }
        }
    }

    fun markRead() {
        markReadUseCase(item.article) {
            onSuccess { event.value = RefreshScreen }
            onFailure {
                showOopsSnackBar()
            }
        }
    }

    companion object {
        const val ARTICLE = "article"
    }
}