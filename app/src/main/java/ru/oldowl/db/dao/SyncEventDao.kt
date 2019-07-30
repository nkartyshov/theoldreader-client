package ru.oldowl.db.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import ru.oldowl.db.model.SyncEvent

@Dao
interface SyncEventDao {

    @Query("select * from sync_event")
    fun findAll() : List<SyncEvent>

    @Insert
    fun save(event: SyncEvent)

    @Delete
    fun delete(event: SyncEvent)
}