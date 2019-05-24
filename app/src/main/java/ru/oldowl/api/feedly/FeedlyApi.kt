package ru.oldowl.api.feedly

import org.jetbrains.anko.AnkoLogger
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Query
import retrofit2.http.Url
import ru.oldowl.api.feedly.model.SubscriptionResponses
import ru.oldowl.db.model.Subscription
import java.util.*

interface FeedlyWebService {

    @GET("/v3/search/feeds")
    fun searchSubscription(@Query("query", encoded = true) query: String,
                           @Query("locale") locale: String,
                           @Query("count") count: Int = 20): Call<SubscriptionResponses>

    companion object {
        const val BASE_URL = "https://cloud.feedly.com"
    }
}

class FeedlyApi(private val feedlyWebService: FeedlyWebService) : AnkoLogger {

    fun searchSubscription(query: String): List<Subscription> {
        val locale = Locale.getDefault().language

        return feedlyWebService.searchSubscription(query, locale)
                .execute()
                .body()
                ?.results
                ?.map {
                    Subscription(
                            id = it.feedId,
                            title = it.title,
                            htmlUrl = it.website,
                            url = it.feedId.removePrefix("feed/"),
                            categoryId = "default")
                } ?: emptyList()
    }
}