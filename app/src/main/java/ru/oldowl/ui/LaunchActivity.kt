package ru.oldowl.ui

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import org.jetbrains.anko.startActivity
import org.koin.standalone.KoinComponent
import org.koin.standalone.inject
import ru.oldowl.R
import ru.oldowl.service.AccountService

class LaunchActivity : AppCompatActivity(), KoinComponent {
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
