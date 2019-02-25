package ru.oldowl.api.model

import java.util.*

data class ContentResponse(var itemId: String,
                           var title: String,
                           var description: String,
                           var link: String,
                           var publishDate: Date = Date())