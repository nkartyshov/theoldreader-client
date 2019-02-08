package ru.oldowl.dao

import android.arch.lifecycle.LiveData
import android.arch.persistence.room.Dao
import android.arch.persistence.room.Query
import android.arch.persistence.room.Transaction
import ru.oldowl.model.Subscription
import ru.oldowl.model.SubscriptionWithUnread

@Dao
interface SubscriptionDao {

    @Query("select * from subscriptions order by title")
    fun observeAll(): LiveData<List<Subscription>>

    @Transaction
    @Query("select s.*, (select count(*) from articles a where a.subscription_id = s.id and a.read = 0) as unread from subscriptions s order by title")
    fun observeAllWithUnread(): LiveData<List<SubscriptionWithUnread>>
}