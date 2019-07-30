package ru.oldowl.db.dao

import androidx.room.*
import ru.oldowl.db.model.Category

@Dao
interface CategoryDao {

    @Query("select * from category")
    fun findAll(): List<Category>

    @Query("select * from category where id = :id")
    fun findById(id: String): Category

    @Query("select 1 from category where id = :id")
    fun exists(id: String): Boolean

    @Insert
    fun save(category: Category)

    @Update
    fun update(category: Category)
}