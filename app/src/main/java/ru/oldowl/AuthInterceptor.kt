package ru.oldowl

import okhttp3.Interceptor
import okhttp3.Response
import ru.oldowl.repository.AccountRepository

class AuthInterceptor(
        private val accountRepository: AccountRepository
) : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        val request = if (accountRepository.hasAccount()) {
            val token = accountRepository.getAuthTokenOrThrow()

            chain.request()
                    .newBuilder()
                    .addHeader("Authorization", "GoogleLogin auth=$token")
                    .build()
        } else {
            chain.request()
        }

        return chain.proceed(request)
    }
}