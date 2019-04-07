package ru.oldowl.db.model

import android.arch.persistence.room.*
import android.os.Parcelable
import kotlinx.android.parcel.Parcelize
import java.util.*

@Parcelize
@Entity(tableName = "articles",
        foreignKeys = [
            ForeignKey(entity = Subscription::class,
                    parentColumns = ["id"],
                    childColumns = ["subscription_id"],
                    onDelete = ForeignKey.CASCADE)
        ],
        indices = [Index("subscription_id")])
data class Article(
        @PrimaryKey(autoGenerate = true)
        var id: Long = 0,
        @ColumnInfo(name = "original_id")
        var originalId: String = "",
        @ColumnInfo(name = "title")
        var title: String,
        @ColumnInfo(name = "description")
        var description: String,
        @ColumnInfo(name = "url")
        var url: String,
        @ColumnInfo(name = "subscription_id")
        var subscriptionId: Long?,
        @ColumnInfo(name = "read")
        var read: Boolean = false,
        @ColumnInfo(name = "favorite")
        var favorite: Boolean = false,
        @ColumnInfo(name = "publish_date")
        var publishDate: Date = Date()
) : Parcelable