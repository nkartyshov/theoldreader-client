package ru.oldowl.dao

import android.arch.persistence.room.Dao
import android.arch.persistence.room.Delete
import android.arch.persistence.room.Insert
import android.arch.persistence.room.Query
import ru.oldowl.model.Event

@Dao
interface EventDao {

    @Query("select * from event")
    fun findAll() : List<Event>

    @Insert
    fun save(event: Event)

    @Delete
    fun delete(event: Event)
}