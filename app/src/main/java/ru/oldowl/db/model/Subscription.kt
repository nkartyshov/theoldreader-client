package ru.oldowl.db.model

import android.arch.persistence.room.*
import android.os.Parcelable
import kotlinx.android.parcel.Parcelize
import java.util.*

@Parcelize
@Entity(tableName = "subscriptions",
        foreignKeys = [ForeignKey(entity = Category::class,
                parentColumns = ["id"],
                childColumns = ["category_id"],
                onDelete = ForeignKey.SET_NULL)],
        indices = [Index("category_id")])
data class Subscription(
        @PrimaryKey(autoGenerate = true)
        var id: Long = 0,
        @ColumnInfo(name = "category_id")
        var categoryId: Long?,
        @ColumnInfo(name = "feed_id")
        var feedId: String? = null,
        @ColumnInfo(name = "title")
        var title: String,
        @ColumnInfo(name = "url")
        var url: String?,
        @ColumnInfo(name = "html_url")
        var htmlUrl: String? = "",
        @ColumnInfo(name = "last_update_date")
        var lastUpdatedDate: Date? = null,
        @ColumnInfo(name = "create_date")
        var createDate: Date? = Date()
) : Parcelable