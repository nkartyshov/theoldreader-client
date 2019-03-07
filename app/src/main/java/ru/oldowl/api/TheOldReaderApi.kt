package ru.oldowl.api

import com.rometools.rome.io.SyndFeedInput
import okhttp3.*
import org.jetbrains.anko.AnkoLogger
import org.jetbrains.anko.error
import ru.oldowl.JsonHelper
import ru.oldowl.api.model.*
import ru.oldowl.model.Article
import java.io.IOException
import java.io.StringReader
import java.util.*

class TheOldReaderApi : AnkoLogger {

    private val httpClient: OkHttpClient = OkHttpClient()

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
                    val authAdapter = JsonHelper.adapter(AuthResponse::class.java)
                    val authResponse = authAdapter.fromJson(it.string())

                    return authResponse?.auth
                }
            }
        } catch (e: IOException) {
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
                    val subscriptionsAdapter = JsonHelper.adapter(SubscriptionsResponse::class.java)
                    val subscriptionsResponse = subscriptionsAdapter.fromJson(responseBody.string())

                    subscriptionsResponse?.let { subscriptionResponse ->
                        return subscriptionResponse.subscriptions.filter { v ->
                            !v.id.contains("sponsored")
                        }
                    }
                }
            }
        } catch (e: IOException) {
            error("Error getting subscription list", e)
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
                httpUrl.addEncodedQueryParameter(NEWER_THAN_PARAM, it.time.toString())
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
                    val adapter = JsonHelper.adapter(ItemsRefResponse::class.java)
                    val itemsRefResponse = adapter.fromJson(responseBody.string())

                    return itemsRefResponse?.itemRefs?.map { it.id } ?: emptyList()
                }
            }
        } catch (e: IOException) {
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

                    return syndFeed.entries.map {entry ->
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
                                publishDate = entry.publishedDate
                        )
                    }
                }
            }
        } catch (e: IOException) {
            error("Error getting content for items $itemIds", e)
        }

        return emptyList()
    }

    // TODO FIXME
    fun updateItem(article: Article) {
        try {
            val httpUrl = HttpUrl.Builder()
                    .scheme(SCHEMA)
                    .host(ENDPOINT)
                    .addPathSegments(UPDATE_ITEMS)
                    .build()



        } catch (e: IOException) {
            error("Error updating item ${article.originalId}", e)
        }
    }

    companion object {
        private const val SCHEMA = "https"

        private const val ENDPOINT = "theoldreader.com"
        private const val CLIENT_LOGIN = "accounts/ClientLogin"
        private const val SUBSCRIPTION_LIST = "reader/api/0/subscription/list"
        private const val ITEM_IDS_LIST = "reader/api/0/stream/items/ids"
        private const val CONTENTS_LIST = "reader/api/0/stream/items/contents"
        private const val UPDATE_ITEMS = "reader/api/0/edit-tag"

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
    }
}

