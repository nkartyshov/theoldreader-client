package ru.oldowl.viewmodel

import android.arch.lifecycle.ViewModel
import android.content.Context
import kotlinx.coroutines.*
import org.koin.standalone.KoinComponent
import ru.oldowl.R
import ru.oldowl.api.TheOldReaderApi
import ru.oldowl.model.Account
import ru.oldowl.service.AccountService
import kotlin.coroutines.CoroutineContext

class LoginViewModel(private val context: Context,
                     private val theOldReaderApi: TheOldReaderApi,
                     private val accountService: AccountService) : ViewModel(), KoinComponent, CoroutineScope {

    private val job = Job()

    override val coroutineContext: CoroutineContext
        get() = job + Dispatchers.Default

    fun authentication(email: String, password: String): Deferred<Boolean> = async {
        val appName = context.getString(R.string.app_name)
        theOldReaderApi.authentication(email, password, appName)

        val authToken = theOldReaderApi.authentication(email, password, appName)
                ?: return@async false
        val account = Account(email, password, authToken)
        accountService.saveAccount(account)

        return@async true
    }

    override fun onCleared() {
        if (job.isActive) {
            job.cancel()
        }
    }
}