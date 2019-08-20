package ru.oldowl.db.model

import android.os.Parcelable
import androidx.room.*
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

        @PrimaryKey
        @ColumnInfo(name = "id")
        var id: String,

        @ColumnInfo(name = "title")
        var title: String,

        @ColumnInfo(name = "description")
        var description: String,

        @ColumnInfo(name = "url")
        var url: String,

        @ColumnInfo(name = "subscription_id")
        var subscriptionId: String? = null,

        @ColumnInfo(name = "read")
        var read: Boolean = false,

        @ColumnInfo(name = "favorite")
        var favorite: Boolean = false,

        @ColumnInfo(name = "deleted")
        var deleted: Boolean = false,

        @ColumnInfo(name = "publish_date")
        var publishDate: Date = Date()
) : Parcelable