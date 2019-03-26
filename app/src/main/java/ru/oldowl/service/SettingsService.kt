package ru.oldowl.service

import android.content.Context
import android.content.SharedPreferences
import android.support.v7.preference.PreferenceManager
import ru.oldowl.R
import java.util.*

class SettingsService(private val context: Context) {
    private val sharedPreferences: SharedPreferences by lazy { PreferenceManager.getDefaultSharedPreferences(context) }

    var lastSyncDate: Date?
        get() {
            val timestamp: Long = sharedPreferences.getLong(LAST_SYNC_DATE, -1)
            return if (timestamp > -1) Date(timestamp) else null
        }
        set(value) {
            value?.let {
                val editor = sharedPreferences.edit()
                editor.putLong(LAST_SYNC_DATE, it.time)
                editor.apply()
            }
        }

    var hideRead: Boolean
        get() {
            val key = context.getString(R.string.key_hide_read)
            return sharedPreferences.getBoolean(key, false)
        }
        set(value) {
            val key = context.getString(R.string.key_hide_read)
            val editor = sharedPreferences.edit()
            editor.putBoolean(key, value)
            editor.apply()
        }

    val autoUpdate: Boolean
        get() {
            val key = context.getString(R.string.key_auto_update)
            return sharedPreferences.getBoolean(key, true)
        }

    val autoUpdatePeriod: Long
        get() {
            val key = context.getString(R.string.key_auto_update_period)
            return sharedPreferences.getString(key, null)?.toLongOrNull() ?: 1
        }

    val autoCleanupUnreadPeriod: Long
        get() {
            val key = context.getString(R.string.key_auto_cleanup_unread_period)
            return sharedPreferences.getString(key, null)?.toLongOrNull() ?: 1
        }

    val autoCleanupReadPeriod: Long
        get() {
            val key = context.getString(R.string.key_auto_cleanup_read_period)
            return sharedPreferences.getString(key, null)?.toLongOrNull() ?: 1
        }

    companion object {
        private const val LAST_SYNC_DATE = "key_last_sync_date"
    }
}