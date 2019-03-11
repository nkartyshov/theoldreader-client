package ru.oldowl.viewmodel

import android.arch.lifecycle.MutableLiveData
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import ru.oldowl.dao.ArticleDao
import ru.oldowl.model.ArticleAndSubscriptionTitle
import java.util.*

// TODO mark read after loading data
class ArticleViewModel(private val articleDao: ArticleDao) : BaseViewModel() {
    var item: ArticleAndSubscriptionTitle? = null

    val title: String?
        get() = item?.article?.title

    val subscriptionTitle: String?
        get() = item?.subscriptionTitle

    val publishDate: Date?
        get() = item?.article?.publishDate

    val favorite: Boolean
        get() = item?.article?.favorite ?: false

    val url: String?
        get() = item?.article?.url

    val updateUi: MutableLiveData<Unit> = MutableLiveData()

    fun getPageContent(): String {
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
            append(item?.article?.description ?: "")
            append("</body>\n")
            append("</html>\n")
        }
    }

    fun toggleFavorite() {
        launch(Dispatchers.Default) {
            item?.article?.let {
                it.favorite = !it.favorite
                articleDao.updateFavoriteState(it.id, it.favorite)

                launch(Dispatchers.Main) {
                    updateUi.value = Unit
                }
            }
        }
    }
}