package ru.oldowl.api.feedly.model

data class SubscriptionResponses(
        var results: List<SubscriptionResponse>
)

data class SubscriptionResponse(
        var feedId: String,
        var title: String,
        var description: String,
        var website: String
)