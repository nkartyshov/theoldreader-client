package ru.oldowl.db.model

import android.arch.persistence.room.ColumnInfo
import android.arch.persistence.room.Embedded
import java.io.Serializable

data class ArticleAndSubscriptionTitle(
    @Embedded
    var article: Article,

    @ColumnInfo(name = "subscription_title")
    var subscriptionTitle: String
) : Serializable