package ru.oldowl.db

import android.arch.persistence.room.*
import android.content.Context
import ru.oldowl.db.dao.ArticleDao
import ru.oldowl.db.dao.CategoryDao
import ru.oldowl.db.dao.EventDao
import ru.oldowl.db.dao.SubscriptionDao
import ru.oldowl.db.model.*
import java.util.*

@Database(entities = [ Subscription::class, Article::class, Category::class, Event::class ],
        version = 1,
        exportSchema = false)
@TypeConverters(DateTypeConverter::class, EventTypeConverter::class)
abstract class AppDatabase : RoomDatabase() {

    abstract fun subscriptionDao(): SubscriptionDao
    abstract fun articleDao(): ArticleDao
    abstract fun categoryDao(): CategoryDao
    abstract fun eventDao(): EventDao

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
    fun toEventType(code: Int?): EventType? {
        return if (code == null) null else EventType.fromInt(code)
    }

    @TypeConverter
    fun toInt(eventType: EventType?): Int? {
        return eventType?.code
    }
}