package ru.oldowl.viewmodel

import ru.oldowl.model.ArticleAndSubscriptionTitle
import java.util.*

class ArticleViewModel : BaseViewModel() {
    var item: ArticleAndSubscriptionTitle? = null

    val title: String?
        get() = item?.article?.title

    val subscriptionTitle: String?
        get() = item?.subscriptionTitle

    val publishDate: Date?
        get() = item?.article?.publishDate

    fun getPageContent(): String {
        return buildString {
            append("<html>\n")
                append("<head>\n")
                    append("<meta charset=\"utf-8\" />\n")
                    append("<meta name='viewport' content='width=device-width'/>\n")
                    append("<style type=\"text/css\">\n")
                        append("* { max-width: 100%; word-break: break-word }\n")
                        append("body { padding: 0 0.2cm 0 0.2cm; }\n")
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
}