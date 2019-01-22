package ru.oldowl.api.model

data class SubscriptionsResponse(var subscriptions: List<SubscriptionResponse>)

data class SubscriptionResponse(var id: String, var title: String, var url: String, var htmlUrl: String)
