package ru.oldowl.api.theoldreader.model

data class SubscriptionsResponse(var subscriptions: List<SubscriptionResponse>)

data class SubscriptionResponse(var id: String,
                                var title: String,
                                var url: String,
                                var htmlUrl: String,
                                var categories: List<CategoryResponse>)

data class CategoryResponse(var id: String,
                            var label: String)