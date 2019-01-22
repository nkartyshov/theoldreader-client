package ru.oldowl.db.entity

import android.arch.persistence.room.ColumnInfo
import android.arch.persistence.room.Entity
import android.arch.persistence.room.PrimaryKey
import java.util.*

@Entity(tableName = "subscriptions")
data class Subscription(
        @PrimaryKey(autoGenerate = true)
        var id: Int = 0,
        @ColumnInfo(name = "feed_id")
        var feedId: String,
        @ColumnInfo(name = "title")
        var title: String,
        @ColumnInfo(name = "url")
        var url: String,
        @ColumnInfo(name = "html_url")
        var htmlUrl: String = "",
        @ColumnInfo(name = "last_update_date")
        var lastUpdatedDate: Date? = null,
        @ColumnInfo(name = "create_date")
        var createDate: Date = Date()
)