package ru.oldowl.viewmodel

import android.app.Application
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.async
import ru.oldowl.R
import ru.oldowl.api.TheOldReaderApi
import ru.oldowl.model.Account
import ru.oldowl.service.AccountService

class LoginViewModel(private val application: Application,
                     private val theOldReaderApi: TheOldReaderApi,
                     private val accountService: AccountService) : BaseViewModel() {

    fun authentication(email: String, password: String): Deferred<Boolean> = async {
        val appName = application.getString(R.string.app_name)
        theOldReaderApi.authentication(email, password, appName)

        val authToken = theOldReaderApi.authentication(email, password, appName)
                ?: return@async false
        val account = Account(email, password, authToken)
        accountService.saveAccount(account)

        return@async true
    }
}