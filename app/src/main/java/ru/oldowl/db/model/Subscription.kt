package ru.oldowl.db.model

import androidx.room.*
import android.os.Parcelable
import kotlinx.android.parcel.Parcelize
import java.util.*

@Parcelize
@Entity(tableName = "subscriptions",
        foreignKeys = [ForeignKey(entity = Category::class,
                parentColumns = ["id"],
                childColumns = ["category_id"],
                onDelete = ForeignKey.SET_NULL,
                onUpdate = ForeignKey.NO_ACTION)],
        indices = [Index("category_id")])
data class Subscription(
        @PrimaryKey
        var id: String,
        @ColumnInfo(name = "category_id")
        var categoryId: String? = null,
        @ColumnInfo(name = "title")
        var title: String,
        @ColumnInfo(name = "url")
        var url: String,
        @ColumnInfo(name = "html_url")
        var htmlUrl: String = ""
) : Parcelable