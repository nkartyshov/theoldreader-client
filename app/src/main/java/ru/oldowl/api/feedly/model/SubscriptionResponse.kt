package ru.oldowl.api.feedly.model

data class SubscriptionResponses(
        var results: List<SubscriptionResponse>
)

data class SubscriptionResponse(
        var feedId: String,
        var title: String? = null,
        var description: String? = null,
        var website: String? = null
)