package ru.oldowl.api.theoldreader

import com.rometools.rome.io.SyndFeedInput
import okhttp3.FormBody
import okhttp3.OkHttpClient
import okhttp3.Request
import org.jetbrains.anko.AnkoLogger
import org.jetbrains.anko.error
import retrofit2.Call
import retrofit2.http.*
import ru.oldowl.api.theoldreader.model.*
import ru.oldowl.extension.epochTime
import java.io.StringReader
import java.util.*
import java.util.concurrent.TimeUnit

private const val CLIENT_LOGIN = "accounts/ClientLogin"

private const val SUBSCRIPTION_LIST = "reader/api/0/subscription/list"
private const val SUBSCRIPTION_ADD = "reader/api/0/subscription/quickadd"
private const val SUBSCRIPTION_UPDATE = "reader/api/0/subscription/edit"

private const val ITEM_IDS_LIST = "reader/api/0/stream/items/ids"
private const val UPDATE_ITEMS = "reader/api/0/edit-tag"
private const val MARK_ALL_READ = "reader/api/0/mark-all-as-read"

private const val CONTENTS_LIST = "reader/api/0/stream/items/contents"

private const val AUTHORIZATION_HEADER = "Authorization"

private const val QUERY_PARAM = "s"
private const val NEWER_THAN_PARAM = "ot"
private const val ITEMS_PARAM = "i"

private const val ITEMS_ADD_PARAM = "a"
private const val ITEMS_REMOVE_PARAM = "r"

private const val COUNT_PARAM = "n"
private const val COUNT_VALUE = 10000

private const val UNREAD_PARAM = "xt"
private const val UNREAD_VALUE = "user/-/state/com.google/read"

private const val OUTPUT_PARAM = "output"
private const val OUTPUT_JSON = "json"
private const val OUTPUT_ATOM = "atom"

interface TheOldReaderWebService {

    @FormUrlEncoded
    @POST(CLIENT_LOGIN)
    fun authentication(@Field("Email") email: String,
                       @Field("Passwd") password: String,
                       @Field("client") appName: String,
                       @Field("accountType") accountType: String = "HOSTED_OR_GOOGLE",
                       @Field(OUTPUT_PARAM) output: String = OUTPUT_JSON): Call<AuthResponse>

    @GET(SUBSCRIPTION_LIST)
    fun getSubscriptions(@Header(AUTHORIZATION_HEADER) token: String,
                         @Query(OUTPUT_PARAM) output: String = OUTPUT_JSON): Call<SubscriptionsResponse>

    @POST(SUBSCRIPTION_ADD)
    fun addSubscription(@Header(AUTHORIZATION_HEADER) token: String,
                        @Query("quickadd") url: String): Call<AddSubscriptionResponse>

    @FormUrlEncoded
    @POST(SUBSCRIPTION_UPDATE)
    fun unsubscribe(@Header(AUTHORIZATION_HEADER) token: String,
                    @Field("ac") action: String = "unsubscribe",
                    @Field(QUERY_PARAM) query: String,
                    @Query(OUTPUT_PARAM) output: String = OUTPUT_JSON): Call<String>

    @GET(ITEM_IDS_LIST)
    fun getItemIds(@Header(AUTHORIZATION_HEADER) token: String,
                   @Query(QUERY_PARAM) query: String,
                   @Query(NEWER_THAN_PARAM) newerThan: String? = null,
                   @Query(COUNT_PARAM) count: Int = COUNT_VALUE,
                   @Query(UNREAD_PARAM) xt: String = UNREAD_VALUE,
                   @Query(OUTPUT_PARAM) output: String = OUTPUT_JSON): Call<ItemsRefResponse>

    @FormUrlEncoded
    @GET(UPDATE_ITEMS)
    fun addArticleState(@Header(AUTHORIZATION_HEADER) token: String,
                        @Field(ITEMS_PARAM) itemIds: List<String>,
                        @Field(ITEMS_ADD_PARAM) state: String,
                        @Field(OUTPUT_PARAM) output: String = OUTPUT_JSON): Call<String>

    @FormUrlEncoded
    @POST(UPDATE_ITEMS)
    fun removeArticleState(@Header(AUTHORIZATION_HEADER) token: String,
                           @Field(ITEMS_PARAM) itemIds: List<String>,
                           @Field(ITEMS_REMOVE_PARAM) state: String,
                           @Field(OUTPUT_PARAM) output: String = OUTPUT_JSON): Call<String>

    @FormUrlEncoded
    @POST(MARK_ALL_READ)
    fun markAllRead(@Header(AUTHORIZATION_HEADER) token: String,
                    @Field(QUERY_PARAM) query: String,
                    @Field("ts") olderThen: String? = null,
                    @Field(OUTPUT_PARAM) output: String = OUTPUT_JSON): Call<String>

    companion object {
        const val BASE_URL = "https://theoldreader.com"
    }
}

class TheOldReaderApi(private val theOldReaderWebService: TheOldReaderWebService,
                      private val httpClient: OkHttpClient) : AnkoLogger {

    fun authentication(email: String, password: String, appName: String): String? {
        return theOldReaderWebService.authentication(email, password, appName)
                .execute()
                .body()
                ?.auth
    }

    fun getSubscriptions(token: String): List<SubscriptionResponse> {
        return theOldReaderWebService.getSubscriptions(authorizationHeader(token))
                .execute()
                .body()
                ?.subscriptions
                ?.filterNot { it.id.contains("sponsored") } ?: emptyList()
    }

    fun addSubscription(url: String, token: String): String {
        val response = theOldReaderWebService.addSubscription(authorizationHeader(token), url)
                .execute()
                .body()

        if (response?.error.isNullOrBlank()) {
            return response?.streamId!!
        }

        error("Error adding the subscription $url, error ${response?.error}")
        return ""
    }

    fun unsubscribe(feedId: String, token: String): Boolean {
        val body = theOldReaderWebService.unsubscribe(
                authorizationHeader(token),
                query = feedId)
                .execute()
                .body() ?: ""

        if (body.isNotBlank()) {
            error("Error unsubscribe from $feedId\n$body")
            return false
        }

        return true

    }

    fun getFavoriteIds(token: String): List<String> {
        return theOldReaderWebService.getItemIds(
                authorizationHeader(token),
                "user/-/state/com.google/starred")
                .execute()
                .body()
                ?.itemRefs?.map { it.id } ?: emptyList()
    }

    fun getItemIds(feedId: String, token: String, onlyUnread: Boolean = true, newerThan: Date? = null): List<String> {
        return theOldReaderWebService.getItemIds(
                authorizationHeader(token),
                feedId,
                newerThan?.epochTime.toString(),
                xt = if (onlyUnread) "user/-/state/com.google/read" else "")
                .execute()
                .body()
                ?.itemRefs?.map { it.id } ?: emptyList()
    }

    fun getContents(itemIds: List<String>, token: String): List<ContentResponse> {
        try {
            val formBody = FormBody.Builder()
            for (itemId in itemIds) {
                formBody.addEncoded(ITEMS_PARAM, addItemIdPrefixIfExists(itemId))
            }

            formBody.addEncoded(OUTPUT_PARAM, OUTPUT_ATOM)

            val url = TheOldReaderWebService.BASE_URL + "/" + CONTENTS_LIST
            val request = Request.Builder()
                    .url(url)
                    .post(formBody.build())
                    .addHeader(AUTHORIZATION_HEADER, authorizationHeader(token))
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
                                feedId = removeReaderPrefix(entry.source.uri),
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

    fun markAllRead(feedId: String?, token: String, olderThen: Date = Date()): Boolean {

        val body = theOldReaderWebService.markAllRead(
                authorizationHeader(token),
                feedId ?: "user/-/state/com.google/reading-list",
                TimeUnit.MILLISECONDS.toNanos(olderThen.time).toString()
        ).execute().body() ?: ""

        if (body.isBlank()) {
            error("Error mark all read\n$body")
            return false
        }

        return true
    }

    fun updateReadState(itemId: String, state: Boolean, token: String): Boolean {
        val authorization = authorizationHeader(token)
        val itemIds = arrayListOf(addItemIdPrefixIfExists(itemId))
        val readState = "user/-/state/com.google/read"

        val response = if (state)
            theOldReaderWebService.addArticleState(authorization, itemIds, readState)
        else theOldReaderWebService.removeArticleState(authorization, itemIds, readState)

        val body = response.execute().body() ?: ""

        if (body.isBlank()) {
            error("Error mark read item $itemIds, $body")
            return false
        }

        return true
    }

    fun updateFavoriteState(itemId: String, state: Boolean, token: String): Boolean {
        val authorization = authorizationHeader(token)
        val itemIds = arrayListOf(if (itemId.startsWith(ITEM_PREFIX)) itemId else ITEM_PREFIX + itemId)
        val readState = "user/-/state/com.google/starred"

        val response = if (state)
            theOldReaderWebService.addArticleState(authorization, itemIds, readState)
        else theOldReaderWebService.removeArticleState(authorization, itemIds, readState)

        val body = response.execute().body() ?: ""

        if (body.isBlank()) {
            error("Error mark favorite item $itemIds, $body")
            return false
        }

        return true
    }

    companion object {
        fun authorizationHeader(token: String) = "GoogleLogin auth=$token"

        fun addItemIdPrefixIfExists(itemId: String) =
                if (itemId.startsWith(ITEM_PREFIX)) itemId else ITEM_PREFIX + itemId

        fun removeReaderPrefix(uri: String) = uri.removePrefix(READER_PREFIX)

        private const val ITEM_PREFIX = "tag:google.com,2005:reader/item/"
        private const val READER_PREFIX = "tag:google.com,2005:reader/"
    }
}
