package ru.oldowl.ui

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_login.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import org.jetbrains.anko.startActivity
import org.koin.standalone.KoinComponent
import org.koin.standalone.inject
import ru.oldowl.R
import ru.oldowl.api.TheOldReaderApi
import kotlin.coroutines.CoroutineContext

class LoginActivity : AppCompatActivity(), KoinComponent, CoroutineScope {
    private val oldReaderService: TheOldReaderApi by inject()

    override val coroutineContext: CoroutineContext
        get() = Dispatchers.Main

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        sign_in.setOnClickListener {
            launch {
                val email = email.text?.toString() ?: ""
                val password = password.text?.toString() ?: ""


                val deferred = async(Dispatchers.Default) {
                    oldReaderService.authentication(email, password, getString(R.string.app_name))
                }

                val authToken = deferred.await()
                if (authToken != null) {
                    //TODO save to account manager

                    startActivity<MainActivity>()
                    finish()
                } else {
                    //TODO show error
                }
            }
        }
    }
}
