package ru.oldowl.repository

import android.content.Context
import android.content.SharedPreferences
import androidx.core.content.edit
import androidx.preference.PreferenceManager
import com.squareup.moshi.Moshi
import ru.oldowl.R
import ru.oldowl.db.model.Account
import java.util.*
import kotlin.properties.ReadWriteProperty
import kotlin.reflect.KProperty

class SettingsStorage(
        private val context: Context,
        moshi: Moshi
) {

    private val sharedPreferences: SharedPreferences
            by lazy { PreferenceManager.getDefaultSharedPreferences(context) }

    var lastSyncDate: Date?
            by DatePreferenceDelegate(sharedPreferences, LAST_SYNC_DATE)

    var hideRead: Boolean
            by BooleanPreferenceDelegate(sharedPreferences,
                    context.getString(R.string.key_hide_read), true)

    var account: Account?
            by AccountPreferenceDelegate(sharedPreferences,
                    context.getString(R.string.key_account),
                    moshi)

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
        override fun getValue(thisRef: Any, property: KProperty<*>): Long = sharedPreferences.getString(key, null)?.toLong()
                ?: defaultValue

        override fun setValue(thisRef: Any, property: KProperty<*>, value: Long) {
            sharedPreferences.edit {
                putString(key, value.toString())
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
            sharedPreferences.edit {
                putBoolean(key, value)
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
            sharedPreferences.edit {
                remove(key)
                value?.let {
                    putLong(key, it.time)
                }
            }
        }
    }

    class AccountPreferenceDelegate(
            private val sharedPreferences: SharedPreferences,
            private val key: String,
            private val moshi: Moshi
    ) : ReadWriteProperty<Any, Account?> {

        private val adapter by lazy { moshi.adapter(Account::class.java).nullSafe() }

        override fun getValue(thisRef: Any, property: KProperty<*>): Account? =
                sharedPreferences.getString(key, null)?.let {
                    adapter.fromJson(it)
                }

        override fun setValue(thisRef: Any, property: KProperty<*>, value: Account?) =
                sharedPreferences.edit {
                    remove(key)

                    value?.let {
                        putString(key, adapter.toJson(it))
                    }
                }
    }
}