package ru.oldowl.dao

import android.arch.lifecycle.LiveData
import android.arch.persistence.room.Dao
import android.arch.persistence.room.Query
import ru.oldowl.model.Category

@Dao
interface CategoryDao {

    @Query("select * from category")
    fun findAll(): LiveData<List<Category>>
}