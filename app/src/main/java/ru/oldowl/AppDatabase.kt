package ru.oldowl

import android.arch.persistence.room.*
import android.content.Context
import ru.oldowl.dao.ArticleDao
import ru.oldowl.dao.CategoryDao
import ru.oldowl.dao.SubscriptionDao
import ru.oldowl.model.Article
import ru.oldowl.model.Category
import ru.oldowl.model.Subscription
import java.util.*

@Database(entities = [ Subscription::class, Article::class, Category::class ],
        version = 1,
        exportSchema = false)
@TypeConverters(DateTypeConverter::class)
abstract class AppDatabase : RoomDatabase() {

    abstract fun subscriptionDao(): SubscriptionDao
    abstract fun articleDao(): ArticleDao
    abstract fun categoryDao(): CategoryDao

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