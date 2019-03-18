package ru.oldowl.service

import android.content.Context
import android.content.SharedPreferences
import android.support.v7.preference.PreferenceManager
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
            return sharedPreferences.getBoolean(HIDE_READ, false)
        }
        set(value) {
            val editor = sharedPreferences.edit()
            editor.putBoolean(HIDE_READ, value)
            editor.apply()
        }

    companion object {
        private const val LAST_SYNC_DATE = "key_last_sync_date"
        private const val HIDE_READ = "key_hide_read"
    }
}