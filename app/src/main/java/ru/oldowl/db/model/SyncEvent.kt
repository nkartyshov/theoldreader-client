package ru.oldowl.db.model

import android.arch.persistence.room.ColumnInfo
import android.arch.persistence.room.Entity
import android.arch.persistence.room.PrimaryKey
import java.util.*

enum class SyncEventType(val code: Int) {
    UPDATE_READ(0),
    UPDATE_FAVORITE(1),
    MARK_ALL_READ(2),
    UNSUBSCRIBE(3);

    companion object {
        fun fromInt(code: Int): SyncEventType? = values().singleOrNull { it.code == code }
    }
}

@Entity(tableName = "sync_event")
data class SyncEvent(
        @PrimaryKey(autoGenerate = true)
        var id: Long = 0,
        @ColumnInfo(name = "event_type")
        var eventType: SyncEventType,
        @ColumnInfo(name = "payload")
        var payload: String? = "",
        @ColumnInfo(name = "created_date")
        var createdDate: Date = Date()) {

    companion object {

        fun markAllRead(feedId: String?) = SyncEvent(eventType = SyncEventType.MARK_ALL_READ, payload = feedId)

        fun unsubscribe(feedId: String) = SyncEvent(eventType = SyncEventType.UNSUBSCRIBE, payload = feedId)

        fun updateFavorite(itemId: String) = SyncEvent(eventType = SyncEventType.UPDATE_FAVORITE, payload = itemId)

        fun updateRead(itemId: String) = SyncEvent(eventType = SyncEventType.UPDATE_READ, payload = itemId)
    }
}