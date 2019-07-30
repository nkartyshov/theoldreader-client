package ru.oldowl.db.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
@Entity(tableName = "category")
data class Category(
        @PrimaryKey
        var id: String,
        @ColumnInfo(name = "title")
        var title: String
) : Parcelable
