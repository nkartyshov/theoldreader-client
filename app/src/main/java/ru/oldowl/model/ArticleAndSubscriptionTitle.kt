package ru.oldowl.model

import android.arch.persistence.room.ColumnInfo
import android.arch.persistence.room.Embedded

data class ArticleAndSubscriptionTitle(
    @Embedded
    var article: Article,

    @ColumnInfo(name = "subscription_title")
    var subscriptionTitle: String
)