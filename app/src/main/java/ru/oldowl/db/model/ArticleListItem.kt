package ru.oldowl.db.model

import androidx.room.ColumnInfo
import androidx.room.Embedded
import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
data class ArticleListItem(
        @Embedded
        var article: Article,

        @ColumnInfo(name = "subscription_title")
        var subscriptionTitle: String?
) : Parcelable