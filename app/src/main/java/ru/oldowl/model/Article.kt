package ru.oldowl.model

import android.arch.persistence.room.ColumnInfo
import android.arch.persistence.room.Entity
import android.arch.persistence.room.ForeignKey
import android.arch.persistence.room.PrimaryKey
import java.io.Serializable
import java.util.*

@Entity(tableName = "articles",
        foreignKeys = [
            ForeignKey(entity = Subscription::class,
                    parentColumns = ["id"],
                    childColumns = ["subscription_id"],
                    onDelete = ForeignKey.CASCADE)
        ])
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
) : Serializable