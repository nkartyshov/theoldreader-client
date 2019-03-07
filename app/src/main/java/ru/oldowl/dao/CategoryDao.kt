package ru.oldowl.dao

import android.arch.lifecycle.LiveData
import android.arch.persistence.room.Dao
import android.arch.persistence.room.Insert
import android.arch.persistence.room.OnConflictStrategy
import android.arch.persistence.room.Query
import ru.oldowl.model.Category

@Dao
interface CategoryDao {

    @Query("select * from category")
    fun findAll(): LiveData<List<Category>>

    @Query("select * from category where label_id = :labelId")
    fun findByLabelId(labelId: String): Category

    @Query("select id from category where label_id = :labelId")
    fun findIdByLabelId(labelId: String): Long

    @Query("select 1 from category where label_id = :labelId")
    fun exists(labelId: String): Boolean

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun save(category: Category): Long
}