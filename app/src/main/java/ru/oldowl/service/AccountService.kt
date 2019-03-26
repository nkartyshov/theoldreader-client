package ru.oldowl.service

import android.content.Context
import android.content.SharedPreferences
import android.preference.PreferenceManager
import ru.oldowl.JsonHelper
import ru.oldowl.db.model.Account

class AccountService(private val context: Context) {
    private val sharedPreferences: SharedPreferences
            by lazy { PreferenceManager.getDefaultSharedPreferences(context) }

    val adapter = JsonHelper.adapter(Account::class.java)

    fun saveAccount(account: Account) {
        val json = adapter.nullSafe().toJson(account)
        sharedPreferences.edit()
                .remove(KEY_ACCOUNT)
                .putString(KEY_ACCOUNT, json)
                .apply()
    }

    fun getAccount(): Account? {
        val json = sharedPreferences.getString(KEY_ACCOUNT, null)

        json?.let {
            return adapter.nullSafe().fromJson(json)
        }

        return null
    }

    fun hasAccount(): Boolean {
        return sharedPreferences.contains(KEY_ACCOUNT)
    }

    companion object {
        private const val KEY_ACCOUNT = "key_account"
    }
}