package ru.oldowl.api.theoldreader

import com.rometools.rome.feed.synd.SyndFeed
import kotlinx.coroutines.Deferred
import retrofit2.http.*
import ru.oldowl.api.theoldreader.model.*
import ru.oldowl.core.extension.toEpochTime
import ru.oldowl.db.model.Category
import timber.log.Timber
import java.util.*
import java.util.concurrent.TimeUnit

private const val CLIENT_LOGIN = "accounts/ClientLogin"

private const val CATEGORY_LIST = "reader/api/0/tag/list"

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
                       @Field(OUTPUT_PARAM) output: String = OUTPUT_JSON): Deferred<AuthResponse>

    @GET(CATEGORY_LIST)
    fun getCategory(@Query(OUTPUT_PARAM) output: String = OUTPUT_JSON): Deferred<CategoriesResponse>

    @GET(SUBSCRIPTION_LIST)
    fun getSubscriptions(@Query(OUTPUT_PARAM) output: String = OUTPUT_JSON): Deferred<SubscriptionsResponse>

    @POST(SUBSCRIPTION_ADD)
    fun addSubscription(@Query("quickadd") url: String): Deferred<AddSubscriptionResponse>

    @FormUrlEncoded
    @POST(SUBSCRIPTION_UPDATE)
    fun unsubscribe(@Field("ac") action: String = "unsubscribe",
                    @Field(QUERY_PARAM) query: String): Deferred<ErrorResponse>

    @GET(ITEM_IDS_LIST)
    fun getItemIds(@Query(QUERY_PARAM) query: String,
                   @Query(NEWER_THAN_PARAM) newerThan: String? = null,
                   @Query(COUNT_PARAM) count: Int = COUNT_VALUE,
                   @Query(UNREAD_PARAM) xt: String = UNREAD_VALUE,
                   @Query(OUTPUT_PARAM) output: String = OUTPUT_JSON): Deferred<ItemsRefResponse>

    @FormUrlEncoded
    @POST(CONTENTS_LIST)
    fun getContents(@Field(ITEMS_PARAM) itemIds: List<String>,
                    @Field(OUTPUT_PARAM) output: String = OUTPUT_ATOM): Deferred<SyndFeed>


    @FormUrlEncoded
    @POST(UPDATE_ITEMS)
    fun addArticleState(@Field(ITEMS_PARAM) itemIds: List<String>,
                        @Field(ITEMS_ADD_PARAM) state: String): Deferred<ErrorResponse>

    @FormUrlEncoded
    @POST(UPDATE_ITEMS)
    fun removeArticleState(@Field(ITEMS_PARAM) itemIds: List<String>,
                           @Field(ITEMS_REMOVE_PARAM) state: String): Deferred<ErrorResponse>

    @FormUrlEncoded
    @POST(MARK_ALL_READ)
    fun markAllRead(@Field(QUERY_PARAM) query: String,
                    @Field("ts") olderThen: String? = null): Deferred<ErrorResponse>

    companion object {
        const val BASE_URL = "https://theoldreader.com"
    }
}

class TheOldReaderApi(private val theOldReaderWebService: TheOldReaderWebService) {

    suspend fun authentication(email: String, password: String, appName: String): String? =
            theOldReaderWebService.authentication(email, password, appName).await().auth


    suspend fun getSubscriptions(): List<SubscriptionResponse> =
            theOldReaderWebService.getSubscriptions()
                    .await()
                    .subscriptions
                    .filterNot { it.id.contains("sponsored") }


    suspend fun addSubscription(url: String): String? =
            theOldReaderWebService.addSubscription(url)
                    .await()
                    .streamId

    suspend fun unsubscribe(feedId: String): Boolean {
        val body = theOldReaderWebService.unsubscribe(query = feedId)
                .await()
                .errors.joinToString()

        if (body.isNotBlank()) {
            Timber.e("Error unsubscribe from $feedId\n$body")
            return false
        }

        return true
    }

    suspend fun getFavoriteIds(): List<String> =
            theOldReaderWebService.getItemIds(STARRED_STATE)
                    .await()
                    .itemRefs.map { it.id }

    suspend fun getItemIds(
            feedId: String,
            onlyUnread: Boolean = true,
            newerThan: Date? = null
    ): List<String> =
            theOldReaderWebService.getItemIds(
                    feedId,
                    newerThan?.toEpochTime(),
                    xt = if (onlyUnread) READ_STATE else ""
            ).await().itemRefs.map { it.id }


    // TODO refactoring
    suspend fun getContents(itemIds: List<String>): List<ContentResponse> {

        if (itemIds.isNullOrEmpty()) {
            return emptyList()
        }

        return theOldReaderWebService.getContents(itemIds)
                .await()
                .entries
                .map { entry ->
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
                }
    }

    suspend fun markAllRead(
            feedId: String? = null,
            olderThen: Date = Date()
    ): Boolean {

        val query = feedId ?: READLING_LIST_STATE
        val olderThenMs = TimeUnit.MILLISECONDS.toNanos(olderThen.time)

        val body = theOldReaderWebService.markAllRead(query, olderThenMs.toString())
                .await()
                .errors
                .joinToString()

        if (body.isBlank()) {
            Timber.e("Error mark all read\n$body")
            return false
        }

        return true
    }

    suspend fun updateReadState(itemId: String, state: Boolean): Boolean {

        val itemIds = arrayListOf(addItemIdPrefixIfExists(itemId))

        val response = if (state)
            theOldReaderWebService.addArticleState(itemIds, READ_STATE)
        else theOldReaderWebService.removeArticleState(itemIds, READ_STATE)

        val body = response.await().errors.joinToString()

        if (body.isBlank()) {
            return true
        }

        Timber.e("Error mark read item $itemIds, $body")
        return false
    }

    suspend fun updateFavoriteState(itemId: String, state: Boolean): Boolean {

        val itemIds = arrayListOf(addItemIdPrefixIfExists(itemId))

        val response = if (state)
            theOldReaderWebService.addArticleState(itemIds, STARRED_STATE)
        else theOldReaderWebService.removeArticleState(itemIds, STARRED_STATE)

        val body = response.await().errors.joinToString()

        if (body.isBlank()) {
            return true
        }

        Timber.e("Error mark favorite item $itemIds, $body")
        return false
    }

    suspend fun getCategories(): List<Category> =
            theOldReaderWebService.getCategory()
                    .await()
                    .tags
                    .filterNot { CATEGORY_EXCLUSIONS.contains(it.id) }
                    .map {
                        Category(
                                id = it.id,
                                title = it.label ?: it.id.removePrefix(CATEGORY_PREFIX)
                        )
                    }

    companion object {

        private fun addItemIdPrefixIfExists(itemId: String) =
                if (itemId.startsWith(ITEM_PREFIX)) itemId else ITEM_PREFIX + itemId

        private const val ITEM_PREFIX = "tag:google.com,2005:reader/item/"
        private const val READER_PREFIX = "tag:google.com,2005:reader/"
        private const val CATEGORY_PREFIX = "user/-/label/"

        private const val STARRED_STATE = "user/-/state/com.google/starred"
        private const val READ_STATE = "user/-/state/com.google/read"
        private const val READLING_LIST_STATE = "user/-/state/com.google/reading-list"

        private val CATEGORY_EXCLUSIONS = arrayListOf(
                "user/-/state/com.google/starred"
        )
    }
}
