package ru.oldowl.service

import android.content.Context
import android.content.SharedPreferences
import android.support.v7.preference.PreferenceManager
import ru.oldowl.R
import java.util.*
import kotlin.properties.ReadWriteProperty
import kotlin.reflect.KProperty

class SettingsService(private val context: Context) {
    private val sharedPreferences: SharedPreferences
            by lazy { PreferenceManager.getDefaultSharedPreferences(context) }

    var lastSyncDate: Date?
            by DatePreferenceDelegate(sharedPreferences, LAST_SYNC_DATE)

    var hideRead: Boolean
            by BooleanPreferenceDelegate(sharedPreferences,
                    context.getString(R.string.key_hide_read), false)

    val autoUpdate: Boolean
            by BooleanPreferenceDelegate(sharedPreferences,
                    context.getString(R.string.key_auto_update), true)

    val autoUpdatePeriod: Long
            by LongPreferenceDelegate(sharedPreferences,
                    context.getString(R.string.key_auto_update_period), -1)

    val autoCleanupUnreadPeriod: Long
            by LongPreferenceDelegate(sharedPreferences,
                    context.getString(R.string.key_auto_cleanup_unread_period), -1)

    val autoCleanupReadPeriod: Long
            by LongPreferenceDelegate(sharedPreferences,
                    context.getString(R.string.key_auto_cleanup_read_period), -1)

    companion object {
        private const val LAST_SYNC_DATE = "key_last_sync_date"
    }

    class LongPreferenceDelegate(
            private val sharedPreferences: SharedPreferences,
            private val key: String,
            private val defaultValue: Long
    ) : ReadWriteProperty<Any, Long> {
        override fun getValue(thisRef: Any, property: KProperty<*>): Long = sharedPreferences.getLong(key, defaultValue)

        override fun setValue(thisRef: Any, property: KProperty<*>, value: Long) {
            with(sharedPreferences.edit()) {
                putLong(key, value)
                apply()
            }
        }
    }

    class BooleanPreferenceDelegate(
            private val sharedPreferences: SharedPreferences,
            private val key: String,
            private val defaultValue: Boolean
    ) : ReadWriteProperty<Any, Boolean> {
        override fun getValue(thisRef: Any, property: KProperty<*>): Boolean = sharedPreferences.getBoolean(key, defaultValue)

        override fun setValue(thisRef: Any, property: KProperty<*>, value: Boolean) {
            with(sharedPreferences.edit()) {
                putBoolean(key, value)
                apply()
            }
        }
    }

    class DatePreferenceDelegate(
            private val sharedPreferences: SharedPreferences,
            private val key: String
    ) : ReadWriteProperty<Any, Date?> {
        override fun getValue(thisRef: Any, property: KProperty<*>): Date? {
            val timestamp: Long = sharedPreferences.getLong(key, -1)
            return if (timestamp > -1) Date(timestamp) else null
        }

        override fun setValue(thisRef: Any, property: KProperty<*>, value: Date?) {
            value?.let {
                with(sharedPreferences.edit()) {
                    val editor = sharedPreferences.edit()
                    editor.putLong(key, value.time)
                    editor.apply()
                }
            }
        }
    }
}