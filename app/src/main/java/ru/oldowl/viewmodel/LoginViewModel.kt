package ru.oldowl.viewmodel

import android.arch.lifecycle.MutableLiveData
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import ru.oldowl.R
import ru.oldowl.api.theoldreader.TheOldReaderApi
import ru.oldowl.service.AccountService

class LoginViewModel(private val appName: String,
                     private val theOldReaderApi: TheOldReaderApi,
                     private val accountService: AccountService) : BaseViewModel() {

    val email: MutableLiveData<String> = MutableLiveData()
    val emailError: MutableLiveData<Int> = MutableLiveData()

    val password: MutableLiveData<String> = MutableLiveData()
    val passwordError: MutableLiveData<Int> = MutableLiveData()

    val authenticationResult: MutableLiveData<Boolean> = MutableLiveData()
    val progress: MutableLiveData<Boolean> = MutableLiveData()

    fun authentication() = launch(Dispatchers.Main) {
        val email = email.value ?: ""
        val password = password.value ?: ""

        progress.value = true
        try {
            emailError.value = null
            if (email.isBlank()) {
                emailError.value = R.string.email_error
            }

            passwordError.value = null
            if (password.isBlank()) {
                passwordError.value = R.string.password_error
            }

            if (emailError.value != null || passwordError.value != null) {
                return@launch
            }

            val authToken = withContext(Dispatchers.Default) {
                theOldReaderApi.authentication(email, password, appName) ?: ""
            }

            if (authToken.isNotBlank()) {
                accountService.saveAccount(email, password, authToken)
                authenticationResult.value = true
            } else {
                authenticationResult.value = false
            }
        } finally {
            progress.value = false
        }
    }
}