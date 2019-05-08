package ru.oldowl.ui

import android.arch.lifecycle.Observer
import android.databinding.DataBindingUtil
import android.os.Bundle
import android.support.design.widget.Snackbar
import org.jetbrains.anko.startActivity
import org.koin.android.viewmodel.ext.android.viewModel
import org.koin.core.parameter.parametersOf
import ru.oldowl.R
import ru.oldowl.databinding.ActivityLoginBinding
import ru.oldowl.core.extension.browse
import ru.oldowl.core.extension.hideSoftKeyboard
import ru.oldowl.core.ui.BaseActivity
import ru.oldowl.viewmodel.LoginViewModel

class LoginActivity : BaseActivity() {
    private val loginViewModel: LoginViewModel by viewModel { parametersOf(getString(R.string.app_name)) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val databinding: ActivityLoginBinding = DataBindingUtil.setContentView(this, R.layout.activity_login)

        databinding.setOnResetPassword {
            browse(RESET_PASSWORD_URL)
        }

        databinding.setOnSingUp {
            browse(SING_UP_URL)
        }

        databinding.setOnSingIn {
            hideSoftKeyboard()
            loginViewModel.authentication()
        }

        loginViewModel
                .authenticationResult
                .observe(this, Observer { result ->
                    result?.let {
                        if (result) {
                            startActivity<MainActivity>()
                            finish()
                        } else {
                            Snackbar.make(databinding.root,
                                    R.string.authentication_error,
                                    Snackbar.LENGTH_LONG).show()
                        }
                    }
                })

        databinding.viewModel = loginViewModel
        databinding.lifecycleOwner = this
    }

    companion object {
        private const val RESET_PASSWORD_URL = "https://theoldreader.com/users/password/new"
        private const val SING_UP_URL = "https://theoldreader.com/users/sign_up"
    }
}
