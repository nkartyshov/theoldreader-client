package ru.oldowl.viewmodel

import androidx.databinding.ObservableField
import androidx.lifecycle.MutableLiveData
import ru.oldowl.R
import ru.oldowl.core.UiEvent.CloseScreen
import ru.oldowl.core.ui.BaseViewModel
import ru.oldowl.repository.NetworkManager
import ru.oldowl.usecase.LoginUseCase

class LoginViewModel(
        private val networkManager: NetworkManager,
        private val loginUseCase: LoginUseCase
) : BaseViewModel() {

    val email: MutableLiveData<String> = MutableLiveData()
    val emailError: MutableLiveData<Int> = MutableLiveData()

    val password: MutableLiveData<String> = MutableLiveData()
    val passwordError: MutableLiveData<Int> = MutableLiveData()

    val progress: MutableLiveData<Boolean> = MutableLiveData(false)

    fun authentication() {
        if (networkManager.isNetworkUnavailable) {
            showLongSnackbar(R.string.network_unavailable_error)
            return
        }

        val email = email.value ?: ""
        val password = password.value ?: ""

        emailError.value = null
        if (email.isBlank()) {
            emailError.value = R.string.email_error
        }

        passwordError.value = null
        if (password.isBlank()) {
            passwordError.value = R.string.password_error
        }

        if (emailError.value != null || passwordError.value != null) {
            return
        }

        progress.value = true

        val param = LoginUseCase.Param(email, password)
        loginUseCase(param) {
            onSuccess {
                event.value = CloseScreen
            }

            onComplete {
                progress.value = false
            }

            onFailure {
                showShortSnackbar(R.string.authentication_error)
            }
        }
    }
}