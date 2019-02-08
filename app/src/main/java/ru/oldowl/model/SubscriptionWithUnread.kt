package ru.oldowl.model

import android.arch.persistence.room.ColumnInfo
import android.arch.persistence.room.Embedded

class SubscriptionWithUnread(
        @Embedded
        var subscription: Subscription,

        @ColumnInfo(name = "unread")
        var unread: Int
)