package ru.oldowl.db.dao

import android.arch.lifecycle.LiveData
import android.arch.persistence.room.Dao
import android.arch.persistence.room.Query
import ru.oldowl.db.entity.Subscription

@Dao
interface SubscriptionDao {

    @Query("select * from subscriptions order by title")
    fun observeAll(): LiveData<List<Subscription>>
}