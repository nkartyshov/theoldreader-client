package ru.oldowl.api.theoldreader.model

import java.util.*

data class ContentResponse(
        var itemId: String,
        var title: String,
        var description: String,
        var link: String,
        var feedId: String?,
        var publishDate: Date = Date()
)