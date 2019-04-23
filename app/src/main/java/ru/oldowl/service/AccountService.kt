package ru.oldowl.service

import android.content.Context
import android.content.SharedPreferences
import android.preference.PreferenceManager
import com.squareup.moshi.Moshi
import ru.oldowl.db.model.Account

class AccountService(private val context: Context,
                     private val moshi: Moshi) {

    private val sharedPreferences: SharedPreferences
            by lazy { PreferenceManager.getDefaultSharedPreferences(context) }

    val accountAdapter by lazy { moshi.adapter(Account::class.java).nullSafe() }

    fun saveAccount(email: String, password: String, authToken: String) {
        val account = Account(email, password, authToken)

        sharedPreferences.edit()
                .remove(KEY_ACCOUNT)
                .putString(KEY_ACCOUNT, accountAdapter.toJson(account))
                .apply()
    }

    fun getAccount(): Account? {
        val json = sharedPreferences.getString(KEY_ACCOUNT, null)

        json?.let {
            return accountAdapter.nullSafe().fromJson(json)
        }

        return null
    }

    fun hasAccount(): Boolean = sharedPreferences.contains(KEY_ACCOUNT)

    companion object {
        private const val KEY_ACCOUNT = "key_account"
    }
}