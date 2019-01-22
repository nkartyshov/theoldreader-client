package ru.oldowl.db.dao

import android.arch.persistence.room.Dao
import android.arch.persistence.room.Query

@Dao
interface ArticleDao {

    @Query("select * from articles")
    fun observeAll()
}