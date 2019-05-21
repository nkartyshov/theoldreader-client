package ru.oldowl.db.dao

import android.arch.lifecycle.LiveData
import android.arch.persistence.room.Dao
import android.arch.persistence.room.Insert
import android.arch.persistence.room.OnConflictStrategy
import android.arch.persistence.room.Query
import ru.oldowl.db.model.Category

@Dao
interface CategoryDao {

    @Query("select * from category")
    fun findAll(): LiveData<List<Category>>

    @Query("select * from category where id = :id")
    fun findById(id: String): Category

    @Query("select 1 from category where id = :id")
    fun exists(id: String): Boolean

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun save(category: Category)
}