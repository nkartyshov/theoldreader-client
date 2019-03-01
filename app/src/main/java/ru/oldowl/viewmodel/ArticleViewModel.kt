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
}