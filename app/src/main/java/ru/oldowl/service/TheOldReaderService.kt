package ru.oldowl.service

import android.content.Context
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import okhttp3.FormBody
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import ru.oldowl.R
import java.io.IOException

class TheOldReaderService(private val context: Context) {

    private val httpClient: OkHttpClient = OkHttpClient()

    private val moshi: Moshi = Moshi.Builder()
            .add(KotlinJsonAdapterFactory::class)
            .build()

    suspend fun authentication(email: String, password: String): String? {
        try {
            val requestBody = FormBody.Builder()
                    .add("client", context.getString(R.string.app_name))
                    .add("accountType", "HOSTED_OR_GOOGLE")
                    .add("output", "json")
                    .add("Email", email)
                    .add("password", password)
                    .build()

            val request = Request.Builder()
                    .url(HOST + CLIENT_LOGIN)
                    .post(requestBody)
                    .build()

            val response: Response = httpClient.newCall(request).execute()

            if (response.isSuccessful) {
                response.body()?.let {
                    val authAdapter = moshi.adapter(AuthResponse::class.java)
                    val authResponse = authAdapter.fromJson(it.string())

                    return authResponse?.auth
                }
            }
        } catch (e: IOException) {
            // TODO print error
        }

        return null
    }

    companion object {
        private const val HOST = "https://theoldreader.com/"
        private const val CLIENT_LOGIN = "accounts/ClientLogin"
    }
}

data class AuthResponse(var sid: String, var lsid: String, var auth: String)

