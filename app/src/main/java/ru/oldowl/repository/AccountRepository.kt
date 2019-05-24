package ru.oldowl.repository

import ru.oldowl.db.model.Account

interface AccountRepository {

    fun saveAccount(email: String, password: String, authToken: String)

    fun getAccount(): Account?

    fun getAccountOrThrow(): Account

    fun getAuthTokenOrThrow(): String

    fun hasAccount(): Boolean

    class AccountRepositoryImpl(
            private val settingsStorage: SettingsStorage
    ) : AccountRepository {

        override fun saveAccount(email: String, password: String, authToken: String) {
            settingsStorage.account = Account(email, password, authToken)
        }

        override fun getAccount(): Account? = settingsStorage.account

        override fun getAccountOrThrow(): Account = settingsStorage.account
                ?: throw IllegalStateException("Account not found or not authorized")

        override fun getAuthTokenOrThrow(): String = getAccountOrThrow().authToken

        override fun hasAccount(): Boolean = settingsStorage.account != null
    }
}