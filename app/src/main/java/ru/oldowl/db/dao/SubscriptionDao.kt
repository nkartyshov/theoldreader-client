package ru.oldowl.db.dao

import android.arch.persistence.room.*
import ru.oldowl.db.model.Subscription
import ru.oldowl.db.model.SubscriptionNavItem

@Dao
interface SubscriptionDao {

    @Query("select * from subscriptions where id = :subscriptionId")
    fun findById(subscriptionId: Long): Subscription

    @Query("select * from subscriptions")
    fun findAll(): List<Subscription>

    @Query("select s.*, (select count(a.id) from articles a where a.read = 0 and a.subscription_id = s.id) as unread from subscriptions s")
    fun fetchNavItems(): List<SubscriptionNavItem>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun save(subscription: Subscription)

    @Delete
    fun delete(subscription: Subscription)

    @Update
    fun update(subscription: Subscription)
}