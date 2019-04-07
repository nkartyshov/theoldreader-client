package ru.oldowl.db.model

import android.arch.persistence.room.ColumnInfo
import android.arch.persistence.room.Embedded
import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
data class ArticleAndSubscriptionTitle(
        @Embedded
        var article: Article,

        @ColumnInfo(name = "subscription_title")
        var subscriptionTitle: String
) : Parcelable