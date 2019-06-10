package ru.oldowl.ui

import android.os.Bundle
import org.koin.standalone.inject
import ru.oldowl.R
import ru.oldowl.core.extension.startActivity
import ru.oldowl.core.ui.BaseActivity
import ru.oldowl.repository.AccountRepository

class LaunchActivity : BaseActivity() {
    private val accountRepository: AccountRepository by inject()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_launch)

        if (accountRepository.hasAccount())
            startActivity<MainActivity>()
        else
            startActivity<LoginActivity>()

        finish()
    }
}
