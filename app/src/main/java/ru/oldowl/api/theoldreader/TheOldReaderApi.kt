package ru.oldowl.api.theoldreader

import com.rometools.rome.io.SyndFeedInput
import com.squareup.moshi.Moshi
import okhttp3.*
import org.jetbrains.anko.AnkoLogger
import org.jetbrains.anko.error
import ru.oldowl.api.theoldreader.model.*
import ru.oldowl.extension.epochTime
import java.io.StringReader
import java.util.*
import java.util.concurrent.TimeUnit

class TheOldReaderApi(private val httpClient: OkHttpClient,
                      private val moshi: Moshi) : AnkoLogger {

    fun authentication(email: String, password: String, appName: String): String? {
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
                    .add(OUTPUT_PARAM, OUTPUT_JSON)
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
        } catch (e: Exception) {
            error("Error authentication in TheOldReader", e)
        }

        return null
    }

    fun getSubscriptions(token: String): List<SubscriptionResponse> {
        try {
            val httpUrl = HttpUrl.Builder()
                    .scheme(SCHEMA)
                    .host(ENDPOINT)
                    .addPathSegments(SUBSCRIPTION_LIST)
                    .addQueryParameter(OUTPUT_PARAM, OUTPUT_JSON)
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

                    subscriptionsResponse?.let { subscriptionResponse ->
                        return subscriptionResponse.subscriptions.filter { v ->
                            !v.id.contains("sponsored")
                        }
                    }
                }
            }
        } catch (e: Exception) {
            error("Error getting subscription list", e)
        }

        return emptyList()
    }

    fun getFavoriteIds(token: String): List<String> {
        try {
            val httpUrl = HttpUrl.Builder()
                    .scheme(SCHEMA)
                    .host(ENDPOINT)
                    .addPathSegments(ITEM_IDS_LIST)
                    .addEncodedQueryParameter(OUTPUT_PARAM, OUTPUT_JSON)
                    .addEncodedQueryParameter("s", "user/-/state/com.google/starred")
                    .build()

            val request = Request.Builder()
                    .url(httpUrl)
                    .get()
                    .addHeader("Authorization", "GoogleLogin auth=$token")
                    .build()

            val response = httpClient.newCall(request).execute()
            if (response.isSuccessful) {
                response.body()?.let { responseBody ->
                    val adapter = moshi.adapter(ItemsRefResponse::class.java)
                    val itemsRefResponse = adapter.fromJson(responseBody.string())

                    return itemsRefResponse?.itemRefs?.map { it.id } ?: emptyList()
                }
            }
        } catch (e: Exception) {
            error("Error getting a favorites", e)
        }

        return emptyList()
    }

    fun getItemIds(feedId: String, token: String, onlyUnread: Boolean = true, newerThan: Date? = null): List<String> {
        try {
            val httpUrl = HttpUrl.Builder()
                    .scheme(SCHEMA)
                    .host(ENDPOINT)
                    .addPathSegments(ITEM_IDS_LIST)
                    .addEncodedQueryParameter(OUTPUT_PARAM, OUTPUT_JSON)
                    .addEncodedQueryParameter(QUERY_PARAM, feedId)
                    .addEncodedQueryParameter(COUNT_PARAM, COUNT_VALUE)

            newerThan?.let {
                httpUrl.addEncodedQueryParameter(NEWER_THAN_PARAM, it.epochTime.toString())
            }

            if (onlyUnread) {
                httpUrl.addEncodedQueryParameter(UNREAD_PARAM, UNREAD_VALUE)
            }

            val request = Request.Builder()
                    .url(httpUrl.build())
                    .get()
                    .addHeader("Authorization", "GoogleLogin auth=$token")
                    .build()

            val response = httpClient.newCall(request).execute()
            if (response.isSuccessful) {
                response.body()?.let { responseBody ->
                    val adapter = moshi.adapter(ItemsRefResponse::class.java)
                    val itemsRefResponse = adapter.fromJson(responseBody.string())

                    return itemsRefResponse?.itemRefs?.map { it.id } ?: emptyList()
                }
            }
        } catch (e: Exception) {
            error("Error getting items ids for $feedId")
        }

        return emptyList()
    }

    fun getContents(itemIds: List<String>, token: String): List<ContentResponse> {
        try {
            val httpUrl = HttpUrl.Builder()
                    .scheme(SCHEMA)
                    .host(ENDPOINT)
                    .addPathSegments(CONTENTS_LIST)
                    .build()

            val formBody = FormBody.Builder()
            for (itemId in itemIds) {
                formBody.addEncoded(ITEMS_PARAM, ITEM_PREFIX + itemId)
            }

            formBody.addEncoded(OUTPUT_PARAM, OUTPUT_ATOM)

            val request = Request.Builder()
                    .url(httpUrl)
                    .post(formBody.build())
                    .addHeader("Authorization", "GoogleLogin auth=$token")
                    .build()

            val response = httpClient.newCall(request).execute()
            if (response.isSuccessful) {
                response.body()?.let { responseBody ->
                    val syndFeed = SyndFeedInput().build(StringReader(responseBody.string()))

                    return syndFeed.entries.map { entry ->
                        val description = if (entry.contents.isEmpty()) {
                            entry.description.value ?: ""
                        } else {
                            entry.contents.joinToString { content -> content.value }
                        }

                        ContentResponse(
                                itemId = entry.uri,
                                title = entry.title,
                                description = description,
                                link = entry.link,
                                feedId = entry.source.uri.removePrefix(READER_PREFIX),
                                publishDate = entry.publishedDate
                        )
                    }
                }
            }
        } catch (e: Exception) {
            error("Error getting content for items $itemIds", e)
        }

        return emptyList()
    }

    fun unsubscribe(feedId: String, token: String): Boolean {
        try {
            val httpUrl = HttpUrl.Builder()
                    .scheme(SCHEMA)
                    .host(ENDPOINT)
                    .addPathSegments(SUBSCRIPTION_UPDATE)
                    .build()

            val formBody = FormBody.Builder()
                    .addEncoded("ac", "unsubscribe")
                    .addEncoded("s", feedId)
                    .build()

            val request = Request.Builder()
                    .url(httpUrl)
                    .post(formBody)
                    .addHeader("Authorization", "GoogleLogin auth=$token")
                    .build()

            val response = httpClient.newCall(request).execute()
            if (response.isSuccessful) {
                val body = response.body()?.string() ?: ""

                if (body.isNotBlank()) {
                    error("Error unsubscribe from $feedId\n$body")
                    return false
                }

                return true
            }
        } catch (e: Exception) {
            error("Error unsubscribe from $feedId", e)
        }

        return false
    }

    fun updateReadState(itemId: String, state: Boolean, token: String): Boolean {
        try {
            val httpUrl = HttpUrl.Builder()
                    .scheme(SCHEMA)
                    .host(ENDPOINT)
                    .addPathSegments(UPDATE_ITEMS)
                    .build()

            val parameterName = if (state) "a" else "r"
            val paramValue = if (itemId.startsWith(ITEM_PREFIX)) itemId else ITEM_PREFIX + itemId

            val formBody = FormBody.Builder()
                    .addEncoded("i", paramValue)
                    .add(parameterName, "user/-/state/com.google/read")
                    .build()

            val request = Request.Builder()
                    .url(httpUrl)
                    .post(formBody)
                    .addHeader("Authorization", "GoogleLogin auth=$token")
                    .build()

            val response = httpClient.newCall(request).execute()
            if (response.isSuccessful) {
                val body = response.body()?.string() ?: ""

                if (body.isBlank()) {
                    error("Error mark read item $itemId\n$body")
                    return false
                }

                return true
            }
        } catch (e: Exception) {
            error("Error mark read item $itemId", e)
        }

        return false
    }

    fun updateFavoriteState(itemId: String, state: Boolean, token: String): Boolean {
        try {
            val httpUrl = HttpUrl.Builder()
                    .scheme(SCHEMA)
                    .host(ENDPOINT)
                    .addPathSegments(UPDATE_ITEMS)
                    .build()

            val parameterName = if (state) "a" else "r"
            val paramValue = if (itemId.startsWith(ITEM_PREFIX)) itemId else ITEM_PREFIX + itemId

            val formBody = FormBody.Builder()
                    .addEncoded("i", paramValue)
                    .addEncoded(parameterName, "user/-/state/com.google/starred")
                    .build()

            val request = Request.Builder()
                    .url(httpUrl)
                    .post(formBody)
                    .addHeader("Authorization", "GoogleLogin auth=$token")
                    .build()

            val response = httpClient.newCall(request).execute()
            if (response.isSuccessful) {
                val body = response.body()?.string() ?: ""

                if (body.isBlank()) {
                    error("Error mark favorite item $itemId\n$body")
                    return false
                }

                return true
            }
        } catch (e: Exception) {
            error("Error mark favorite item $itemId", e)
        }

        return false
    }

    fun markAllRead(feedId: String?, token: String, olderThen: Date = Date()): Boolean {
        try {
            val httpUrl = HttpUrl.Builder()
                    .scheme(SCHEMA)
                    .host(ENDPOINT)
                    .addPathSegments(MARK_ALL_READ)
                    .build()

            val formBody = FormBody.Builder()
                    .addEncoded("s", feedId ?: "user/-/state/com.google/reading-list")
                    .addEncoded("ts", TimeUnit.MILLISECONDS.toNanos(olderThen.time).toString())
                    .build()

            val request = Request.Builder()
                    .url(httpUrl)
                    .post(formBody)
                    .addHeader("Authorization", "GoogleLogin auth=$token")
                    .build()

            val response = httpClient.newCall(request).execute()
            if (response.isSuccessful) {
                val body = response.body()?.string() ?: ""

                if (body.isBlank()) {
                    error("Error mark all read\n$body")
                    return false
                }

                return true
            }
        } catch (e: Exception) {
            error("Error mark all read", e)
        }

        return false
    }

    companion object {
        private const val SCHEMA = "https"
        private const val ENDPOINT = "theoldreader.com"

        private const val CLIENT_LOGIN = "accounts/ClientLogin"

        private const val SUBSCRIPTION_LIST = "reader/api/0/subscription/list"
        private const val SUBSCRIPTION_UPDATE = "reader/api/0/subscription/edit"

        private const val ITEM_IDS_LIST = "reader/api/0/stream/items/ids"
        private const val UPDATE_ITEMS = "reader/api/0/edit-tag"
        private const val MARK_ALL_READ = "reader/api/0/mark-all-as-read"

        private const val CONTENTS_LIST = "reader/api/0/stream/items/contents"

        private const val QUERY_PARAM = "s"
        private const val NEWER_THAN_PARAM = "ot"
        private const val ITEMS_PARAM = "i"

        private const val COUNT_PARAM = "n"
        private const val COUNT_VALUE = "10000"

        private const val UNREAD_PARAM = "xt"
        private const val UNREAD_VALUE = "user/-/state/com.google/read"

        private const val OUTPUT_PARAM = "output"
        private const val OUTPUT_JSON = "json"
        private const val OUTPUT_ATOM = "atom"

        private const val ITEM_PREFIX = "tag:google.com,2005:reader/item/"
        private const val READER_PREFIX = "tag:google.com,2005:reader/"
    }
}
