package ru.oldowl.ui

import android.os.Bundle
import org.jetbrains.anko.startActivity
import org.koin.standalone.inject
import ru.oldowl.R
import ru.oldowl.core.ui.BaseActivity
import ru.oldowl.service.AccountService

class LaunchActivity : BaseActivity() {
    private val accountService: AccountService by inject()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_launch)

        if (accountService.hasAccount())
            startActivity<MainActivity>()
        else
            startActivity<LoginActivity>()

        finish()
    }
}
