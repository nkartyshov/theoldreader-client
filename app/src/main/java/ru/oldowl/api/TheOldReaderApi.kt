package ru.oldowl.api

import com.rometools.rome.io.SyndFeedInput
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import okhttp3.*
import org.jetbrains.anko.AnkoLogger
import org.jetbrains.anko.error
import ru.oldowl.api.model.AuthResponse
import ru.oldowl.api.model.SubscriptionsResponse
import ru.oldowl.model.Article
import ru.oldowl.model.Subscription
import java.io.IOException
import java.io.StringReader

class TheOldReaderApi : AnkoLogger {

    private val httpClient: OkHttpClient = OkHttpClient()

    private val moshi: Moshi = Moshi.Builder()
            .add(KotlinJsonAdapterFactory())
            .build()

    suspend fun authentication(email: String, password: String, appName: String): String? {
        try {
            val httpUrl = HttpUrl.Builder()
                    .scheme(SCHEMA)
                    .host(ENDPOINT)
                    .addPathSegments(CLIENT_LOGIN)
                    .build()

            val requestBody = FormBody.Builder()
                    .add("client", appName)
                    .add("accountType", "HOSTED_OR_GOOGLE")
                    .add("Email", email)
                    .add("Passwd", password)
                    .add(OUTPUT_PARAM, OUTPUT_VALUE)
                    .build()

            val request = Request.Builder()
                    .url(httpUrl)

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
            error("Error authentication in TheOldReader", e)
        }

        return null
    }

    suspend fun getSubscriptions(token: String): List<Subscription> {
        val result = ArrayList<Subscription>()

        try {
            val httpUrl = HttpUrl.Builder()
                    .scheme(SCHEMA)
                    .host(ENDPOINT)
                    .addPathSegments(SUBSCRIPTION_LIST)
                    .addQueryParameter(OUTPUT_PARAM, OUTPUT_VALUE)
                    .build()

            val request = Request.Builder()
                    .url(httpUrl)
                    .get()
                    .addHeader("Authorization", "GoogleLogin auth=$token")
                    .build()

            val response = httpClient.newCall(request).execute()
            if (response.isSuccessful) {
                response.body()?.let { responseBody ->
                    val subscriptionsAdapter = moshi.adapter(SubscriptionsResponse::class.java)
                    val subscriptionsResponse = subscriptionsAdapter.fromJson(responseBody.string())

                    subscriptionsResponse?.let {
                        for (subscriptionResponse in it.subscriptions) {
                            if (!subscriptionResponse.id.contains("sponsored")) {
                                val subscription = Subscription(
                                        feedId = subscriptionResponse.id,
                                        title = subscriptionResponse.title,
                                        url = subscriptionResponse.url,
                                        htmlUrl = subscriptionResponse.htmlUrl
                                )

                                result.add(subscription)
                            }
                        }
                    }

                }
            }
        } catch (e: IOException) {
            error("Error getting subscription list", e)
        }

        return result
    }

    suspend fun getArticles(subscription: Subscription, token: String): List<Article> {
        val result = ArrayList<Article>()

        try {
            val httpUrl = HttpUrl.Builder()
                    .scheme(SCHEMA)
                    .host(ENDPOINT)
                    .addPathSegments(ITEMS_STREAM)
                    .addPathSegments(subscription.feedId)
                    .addEncodedQueryParameter(UNREAD_PARAM, UNREAD_VALUE)
                    .build()

            val request = Request.Builder()
                    .url(httpUrl)
                    .get()
                    .addHeader("Authorization", "GoogleLogin auth=$token")
                    .build()

            val response = httpClient.newCall(request).execute()
            if (response.isSuccessful) {
                response.body()?.let {
                    val syndFeed = SyndFeedInput().build(StringReader(it.string()))
                    for (entry in syndFeed.entries) {
                        val description = if (entry.contents.isEmpty()) {
                            entry.description.value ?: ""
                        } else {
                            entry.contents.joinToString { content -> content.value }
                        }

                        val article = Article(
                                originalId = entry.uri,
                                title = entry.title,
                                description = description,
                                publishDate = entry.publishedDate,
                                subscriptionId = subscription.id
                        )

                        result.add(article)
                    }
                }
            }
        } catch (e: IOException) {
            error("Error getting article for ${subscription.feedId}(${subscription.feedId})", e)
        }

        return result
    }

    companion object {
        private const val SCHEMA = "https"

        private const val ENDPOINT = "theoldreader.com"
        private const val CLIENT_LOGIN = "accounts/ClientLogin"
        private const val SUBSCRIPTION_LIST = "reader/api/0/subscription/list"
        private const val ITEMS_STREAM = "reader/atom/"

        private const val UNREAD_PARAM = "xt"
        private const val UNREAD_VALUE = "user/-/state/com.google/read"
        private const val OUTPUT_PARAM = "output"
        private const val OUTPUT_VALUE = "json"
    }
}

