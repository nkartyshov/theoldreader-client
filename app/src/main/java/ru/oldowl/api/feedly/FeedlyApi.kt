package ru.oldowl.api.feedly

import kotlinx.coroutines.Deferred
import retrofit2.http.GET
import retrofit2.http.Query
import ru.oldowl.api.feedly.model.SubscriptionResponses
import ru.oldowl.db.model.Subscription
import java.util.*

interface FeedlyWebService {

    @GET("/v3/search/feeds")
    fun searchSubscription(@Query("query", encoded = true) query: String,
                           @Query("locale") locale: String,
                           @Query("count") count: Int = 20): Deferred<SubscriptionResponses>

    companion object {
        const val BASE_URL = "https://cloud.feedly.com"
    }
}

class FeedlyApi(private val feedlyWebService: FeedlyWebService) {

    suspend fun searchSubscription(query: String): List<Subscription> {
        val locale = Locale.getDefault().language

        return feedlyWebService.searchSubscription(query, locale)
                .await()
                .results
                .map {
                    val url = it.feedId.removePrefix("feed/")

                    Subscription(
                            id = it.feedId,
                            url = url,
                            title = it.title ?: url,
                            htmlUrl = it.website ?: url)
                }
    }
}