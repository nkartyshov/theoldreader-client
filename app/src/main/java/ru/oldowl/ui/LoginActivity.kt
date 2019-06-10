package ru.oldowl.ui

import android.databinding.DataBindingUtil
import android.os.Bundle
import org.koin.android.viewmodel.ext.android.viewModel
import ru.oldowl.R
import ru.oldowl.core.CloseScreen
import ru.oldowl.core.Failure
import ru.oldowl.core.extension.*
import ru.oldowl.core.ui.BaseActivity
import ru.oldowl.databinding.ActivityLoginBinding
import ru.oldowl.viewmodel.LoginViewModel

class LoginActivity : BaseActivity() {
    private val viewModel: LoginViewModel by viewModel()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        DataBindingUtil.setContentView<ActivityLoginBinding>(this, R.layout.activity_login).also {
            it.viewModel = viewModel
            it.lifecycleOwner = this

            it.setOnResetPassword {
                browse(RESET_PASSWORD_URL)
            }

            it.setOnSingUp {
                browse(SING_UP_URL)
            }

            it.setOnSingIn {
                hideSoftKeyboard()
                viewModel.authentication()
            }

            observe(viewModel.event) { event ->
                when (event) {
                    is Failure -> showFailure(it.root, event)
                    is CloseScreen -> {
                        startActivity<MainActivity>()
                        finish()
                    }
                }
            }
        }
    }

    companion object {
        private const val RESET_PASSWORD_URL = "https://theoldreader.com/users/password/new"
        private const val SING_UP_URL = "https://theoldreader.com/users/sign_up"
    }
}
