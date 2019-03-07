package ru.oldowl.model

import android.arch.persistence.room.ColumnInfo
import android.arch.persistence.room.Entity
import android.arch.persistence.room.ForeignKey
import android.arch.persistence.room.PrimaryKey
import java.io.Serializable
import java.util.*

@Entity(tableName = "subscriptions",
        foreignKeys = [ForeignKey(entity = Category::class,
                parentColumns = ["id"],
                childColumns = ["category_id"],
                onDelete = ForeignKey.SET_NULL)])
data class Subscription(
        @PrimaryKey(autoGenerate = true)
        var id: Long = 0,
        @ColumnInfo(name = "category_id")
        var categoryId: Long?,
        @ColumnInfo(name = "feed_id")
        var feedId: String?,
        @ColumnInfo(name = "title")
        var title: String?,
        @ColumnInfo(name = "url")
        var url: String?,
        @ColumnInfo(name = "html_url")
        var htmlUrl: String? = "",
        @ColumnInfo(name = "last_update_date")
        var lastUpdatedDate: Date? = null,
        @ColumnInfo(name = "create_date")
        var createDate: Date? = Date()
) : Serializable