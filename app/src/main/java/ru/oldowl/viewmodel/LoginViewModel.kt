package ru.oldowl.viewmodel

import kotlinx.coroutines.async
import ru.oldowl.api.TheOldReaderApi
import ru.oldowl.db.model.Account
import ru.oldowl.service.AccountService

class LoginViewModel(private val appName: String,
                     private val theOldReaderApi: TheOldReaderApi,
                     private val accountService: AccountService) : BaseViewModel() {

    suspend fun authentication(email: String, password: String): String? = async {
        theOldReaderApi.authentication(email, password, appName)

        return@async theOldReaderApi.authentication(email, password, appName)
    }.await()

    fun saveAccount(email: String, password: String, authToken: String) {
        val account = Account(email, password, authToken)
        accountService.saveAccount(account)
    }
}