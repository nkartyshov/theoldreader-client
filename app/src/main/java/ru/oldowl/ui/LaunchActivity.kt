package ru.oldowl.ui

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import org.jetbrains.anko.startActivity
import org.koin.standalone.KoinComponent
import ru.oldowl.R

class LaunchActivity : AppCompatActivity(), KoinComponent {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_launch)

        startActivity<LoginActivity>()
        finish()
    }
}
