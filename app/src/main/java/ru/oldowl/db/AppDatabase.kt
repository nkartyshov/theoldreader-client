package ru.oldowl.db

import androidx.room.*
import android.content.Context
import ru.oldowl.db.dao.ArticleDao
import ru.oldowl.db.dao.CategoryDao
import ru.oldowl.db.dao.SyncEventDao
import ru.oldowl.db.dao.SubscriptionDao
import ru.oldowl.db.model.*
import java.util.*

@Database(entities = [ Subscription::class, Article::class, Category::class, SyncEvent::class ],
        version = 2,
        exportSchema = false)
@TypeConverters(DateTypeConverter::class, EventTypeConverter::class)
abstract class AppDatabase : RoomDatabase() {

    abstract fun subscriptionDao(): SubscriptionDao
    abstract fun articleDao(): ArticleDao
    abstract fun categoryDao(): CategoryDao
    abstract fun eventDao(): SyncEventDao

    companion object {
        internal fun buildDatabase(context: Context) =
                Room.databaseBuilder(context.applicationContext,
                        AppDatabase::class.java, "old_owl_database.db")
                        .build()
    }
}

class DateTypeConverter {

    @TypeConverter
    fun toDate(timestamp: Long?): Date? {
        return if (timestamp == null) null else Date(timestamp)
    }

    @TypeConverter
    fun toTimestamp(date: Date?): Long? {
        return date?.time
    }
}

class EventTypeConverter {

    @TypeConverter
    fun toEventType(code: Int?): SyncEventType? {
        return if (code == null) null else SyncEventType.fromInt(code)
    }

    @TypeConverter
    fun toInt(eventType: SyncEventType?): Int? {
        return eventType?.code
    }
}