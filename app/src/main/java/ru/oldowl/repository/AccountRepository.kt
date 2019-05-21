package ru.oldowl.repository

import ru.oldowl.db.model.Account

class AccountRepository(
        private val settingsStorage: SettingsStorage
) {

    fun saveAccount(email: String, password: String, authToken: String) {
        settingsStorage.account = Account(email, password, authToken)
    }

    fun getAccount(): Account? = settingsStorage.account

    fun getAccountOrThrow(): Account = settingsStorage.account
            ?: throw IllegalStateException("Account not found or not authorized")

    fun hasAccount(): Boolean = settingsStorage.account != null
}