package ru.oldowl.ui

import android.os.Bundle
import android.support.design.widget.Snackbar
import kotlinx.android.synthetic.main.activity_login.*
import kotlinx.coroutines.launch
import org.jetbrains.anko.startActivity
import org.koin.standalone.inject
import ru.oldowl.R
import ru.oldowl.extension.openWebsite
import ru.oldowl.viewmodel.LoginViewModel

class LoginActivity : BaseActivity() {
    private val loginViewModel: LoginViewModel by inject()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        sign_in.setOnClickListener {
            val emailValue = email.text?.toString() ?: ""
            val passwordValue = password.text?.toString() ?: ""

            if (emailValue.isBlank()) {
                email.error = getString(R.string.email_error)
            }

            if (passwordValue.isBlank()) {
                password.error = getString(R.string.password_error)
            }

            launch {
                val deferred = loginViewModel.authentication(emailValue, passwordValue)

                if (deferred.await()) {
                    startActivity<MainActivity>()
                    finish()
                } else {
                    Snackbar.make(it, getString(R.string.authentication_error), Snackbar.LENGTH_LONG).show()
                }
            }
        }

        reset_password.setOnClickListener {
            openWebsite(RESET_PASSWORD_URL)
        }

        sing_up.setOnClickListener {
            openWebsite(SING_UP_URL)
        }
    }

    companion object {
        private const val RESET_PASSWORD_URL = "https://theoldreader.com/users/password/new"
        private const val SING_UP_URL = "https://theoldreader.com/users/sign_up"
    }
}
