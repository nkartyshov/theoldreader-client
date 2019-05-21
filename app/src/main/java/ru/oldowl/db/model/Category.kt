package ru.oldowl.db.model

import android.arch.persistence.room.ColumnInfo
import android.arch.persistence.room.Entity
import android.arch.persistence.room.PrimaryKey
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
