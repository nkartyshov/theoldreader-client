package ru.oldowl.db.model

import android.arch.persistence.room.ColumnInfo
import android.arch.persistence.room.Entity
import android.arch.persistence.room.PrimaryKey
import java.util.*

enum class EventType(val code: Int) {
    UPDATE_READ(0),
    UPDATE_FAVORITE(1),
    MARK_ALL_READ(2),
    UNSUBSCRIBE(3);

    companion object {
        fun fromInt(code: Int): EventType? = values().singleOrNull { it.code == code }
    }
}

@Entity(tableName = "event")
data class Event(
        @PrimaryKey(autoGenerate = true)
        var id: Long = 0,
        @ColumnInfo(name = "event_type")
        var eventType: EventType,
        @ColumnInfo(name = "payload")
        var payload: String? = "",
        @ColumnInfo(name = "created_date")
        var createdDate: Date = Date())