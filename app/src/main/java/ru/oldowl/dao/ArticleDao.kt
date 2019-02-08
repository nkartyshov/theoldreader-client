package ru.oldowl.dao

import android.arch.lifecycle.LiveData
import android.arch.persistence.room.Dao
import android.arch.persistence.room.Query
import ru.oldowl.model.Article

@Dao
interface ArticleDao {

    @Query("select * from articles")
    fun observeAll(): LiveData<List<Article>>
}