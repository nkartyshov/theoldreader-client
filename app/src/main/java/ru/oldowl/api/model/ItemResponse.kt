package ru.oldowl.api.model

data class ItemsRefResponse(
        var itemRefs: List<ItemRefResponse>
)

data class ItemRefResponse(
        var id: String,
        var directStreamIds: List<String>,
        var timestampUsec: Long
)

