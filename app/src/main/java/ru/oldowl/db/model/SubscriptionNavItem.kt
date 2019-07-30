package ru.oldowl.db.model

import androidx.room.ColumnInfo
import androidx.room.Embedded

data class SubscriptionNavItem(
        @Embedded
        var subscription: Subscription,

        @ColumnInfo(name = "unread")
        var unread: Int
)