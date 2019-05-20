package ru.oldowl.usecase

import ru.oldowl.api.feedly.FeedlyApi
import ru.oldowl.core.Result
import ru.oldowl.core.UseCase
import ru.oldowl.db.model.Subscription

class SearchSubscriptionUseCase(
        private val feedlyApi: FeedlyApi
) : UseCase<String, List<Subscription>>() {

    override suspend fun run(param: String): Result<List<Subscription>> =
            Result.success(feedlyApi.searchSubscription(param))

}