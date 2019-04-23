package ru.oldowl.api.theoldreader.model

data class AddSubscriptionResponse(
        val query: String,
        val numResults: Int,
        val streamId: String,
        val error: String
)