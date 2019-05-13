package ru.oldowl.db.dao

import android.arch.persistence.room.Dao
import android.arch.persistence.room.Delete
import android.arch.persistence.room.Insert
import android.arch.persistence.room.Query
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