package ru.oldowl.db.model

import android.arch.persistence.room.ColumnInfo
import android.arch.persistence.room.Embedded

data class SubscriptionAndUnreadCount(
        @Embedded
        var subscription: Subscription,

        @ColumnInfo(name = "unread")
        var unread: Int
)