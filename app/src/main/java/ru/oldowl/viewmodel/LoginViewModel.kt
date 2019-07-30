package ru.oldowl.viewmodel

import androidx.lifecycle.MutableLiveData
import androidx.databinding.ObservableField
import kotlinx.coroutines.launch
import ru.oldowl.R
import ru.oldowl.core.UiEvent.CloseScreen
import ru.oldowl.core.UiEvent.ShowSnackbar
import ru.oldowl.core.ui.BaseViewModel
import ru.oldowl.usecase.LoginUseCase

class LoginViewModel(private val loginUseCase: LoginUseCase) : BaseViewModel() {

    val email: MutableLiveData<String> = MutableLiveData()
    val emailError: MutableLiveData<Int> = MutableLiveData()

    val password: MutableLiveData<String> = MutableLiveData()
    val passwordError: MutableLiveData<Int> = MutableLiveData()

    val progress: ObservableField<Boolean> = ObservableField(false)

    fun authentication() = launch {
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
            return@launch
        }

        progress.set(true)

        val param = LoginUseCase.Param(email, password)
        loginUseCase(param) {
            onSuccess {
                event.value = CloseScreen
            }

            onComplete {
                progress.set(false)
            }

            onFailure {
                event.value = ShowSnackbar(R.string.authentication_error)
            }
        }
    }
}