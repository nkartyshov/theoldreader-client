package ru.oldowl.api.theoldreader

import com.rometools.rome.feed.synd.SyndFeed
import retrofit2.Call
import retrofit2.http.*
import ru.oldowl.api.theoldreader.model.*
import ru.oldowl.core.extension.epochTime
import ru.oldowl.db.model.Category
import timber.log.Timber
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
                    @Field(QUERY_PARAM) query: String): Call<ErrorResponse>

    @GET(ITEM_IDS_LIST)
    fun getItemIds(@Header(AUTHORIZATION_HEADER) token: String,
                   @Query(QUERY_PARAM) query: String,
                   @Query(NEWER_THAN_PARAM) newerThan: String? = null,
                   @Query(COUNT_PARAM) count: Int = COUNT_VALUE,
                   @Query(UNREAD_PARAM) xt: String = UNREAD_VALUE,
                   @Query(OUTPUT_PARAM) output: String = OUTPUT_JSON): Call<ItemsRefResponse>

    @FormUrlEncoded
    @POST(CONTENTS_LIST)
    fun getContents(@Header(AUTHORIZATION_HEADER) token: String,
                    @Field(ITEMS_PARAM) itemIds: List<String>,
                    @Field(OUTPUT_PARAM) output: String = OUTPUT_ATOM): Call<SyndFeed>


    @FormUrlEncoded
    @POST(UPDATE_ITEMS)
    fun addArticleState(@Header(AUTHORIZATION_HEADER) token: String,
                        @Field(ITEMS_PARAM) itemIds: List<String>,
                        @Field(ITEMS_ADD_PARAM) state: String): Call<ErrorResponse>

    @FormUrlEncoded
    @POST(UPDATE_ITEMS)
    fun removeArticleState(@Header(AUTHORIZATION_HEADER) token: String,
                           @Field(ITEMS_PARAM) itemIds: List<String>,
                           @Field(ITEMS_REMOVE_PARAM) state: String): Call<ErrorResponse>

    @FormUrlEncoded
    @POST(MARK_ALL_READ)
    fun markAllRead(@Header(AUTHORIZATION_HEADER) token: String,
                    @Field(QUERY_PARAM) query: String,
                    @Field("ts") olderThen: String? = null): Call<ErrorResponse>

    companion object {
        const val BASE_URL = "https://theoldreader.com"
    }
}

class TheOldReaderApi(private val theOldReaderWebService: TheOldReaderWebService) {

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

    fun addSubscription(url: String, token: String): String? {

        val response = theOldReaderWebService.addSubscription(authorizationHeader(token), url)
                .execute()
                .body()

        if (response?.error.isNullOrBlank()) {
            return response?.streamId!!
        }

        Timber.e("Error adding the subscription $url, error ${response?.error}")
        return null
    }

    fun unsubscribe(feedId: String, token: String): Boolean {

        val body = theOldReaderWebService.unsubscribe(
                authorizationHeader(token),
                query = feedId)
                .execute()
                .body()
                ?.errors
                ?.joinToString() ?: ""

        if (body.isNotBlank()) {
            Timber.e("Error unsubscribe from $feedId\n$body")
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

        if (itemIds.isNullOrEmpty()) {
            return emptyList()
        }

        return theOldReaderWebService.getContents(authorizationHeader(token), itemIds)
                .execute()
                .body()
                ?.entries
                ?.map { entry ->
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
                            feedId = entry.source?.uri?.removePrefix(READER_PREFIX),
                            publishDate = entry.publishedDate
                    )
                } ?: emptyList()
    }

    fun markAllRead(feedId: String? = null, token: String, olderThen: Date = Date()): Boolean {

        val body = theOldReaderWebService.markAllRead(
                authorizationHeader(token),
                feedId ?: "user/-/state/com.google/reading-list",
                TimeUnit.MILLISECONDS.toNanos(olderThen.time).toString()
        ).execute().body()?.errors?.joinToString() ?: ""

        if (body.isBlank()) {
            Timber.e("Error mark all read\n$body")
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

        val body = response.execute().body()?.errors?.joinToString()

        if (body.isNullOrBlank()) {
            return true
        }

        Timber.e("Error mark read item $itemIds, $body")
        return false
    }

    fun updateFavoriteState(itemId: String, state: Boolean, token: String): Boolean {

        val authorization = authorizationHeader(token)
        val itemIds = arrayListOf(addItemIdPrefixIfExists(itemId))
        val readState = "user/-/state/com.google/starred"

        val response = if (state)
            theOldReaderWebService.addArticleState(authorization, itemIds, readState)
        else theOldReaderWebService.removeArticleState(authorization, itemIds, readState)

        val body = response.execute().body()?.errors?.joinToString()

        if (body.isNullOrBlank()) {
            return true
        }

        Timber.e("Error mark favorite item $itemIds, $body")
        return false
    }

    fun getCategories(authToken: String): List<Category> =
            theOldReaderWebService.getSubscriptions(authorizationHeader(authToken))
                    .execute()
                    .body()
                    ?.subscriptions
                    ?.flatMap { it.categories }
                    ?.map {
                        Category(
                                id = it.id,
                                title = it.label
                        )
                    } ?: emptyList()

    companion object {
        private fun authorizationHeader(token: String) = "GoogleLogin auth=$token"

        private fun addItemIdPrefixIfExists(itemId: String) =
                if (itemId.startsWith(ITEM_PREFIX)) itemId else ITEM_PREFIX + itemId

        private const val ITEM_PREFIX = "tag:google.com,2005:reader/item/"
        private const val READER_PREFIX = "tag:google.com,2005:reader/"
    }
}
