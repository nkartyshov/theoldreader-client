package ru.oldowl.dao

import android.arch.persistence.room.Dao
import android.arch.persistence.room.Insert
import android.arch.persistence.room.OnConflictStrategy
import android.arch.persistence.room.Query
import ru.oldowl.model.Subscription
import ru.oldowl.model.SubscriptionAndUnreadCount

@Dao
interface SubscriptionDao {

    @Query("select * from subscriptions where id = :subscriptionId")
    fun findById(subscriptionId: Long): Subscription

    @Query("select * from subscriptions")
    fun findAll(): List<Subscription>

    @Query("select s.*, (select count(a.id) from articles a where a.read = 0 and a.subscription_id = s.id) as unread from subscriptions s")
    fun findAllWithUnread(): List<SubscriptionAndUnreadCount>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun save(subscription: Subscription): Long
}