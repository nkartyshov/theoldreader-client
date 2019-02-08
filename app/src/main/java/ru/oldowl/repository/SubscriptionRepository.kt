package ru.oldowl.repository

import android.arch.lifecycle.LiveData
import ru.oldowl.dao.SubscriptionDao
import ru.oldowl.model.Subscription
import ru.oldowl.model.SubscriptionWithUnread

class SubscriptionRepository(private val subscriptionDao: SubscriptionDao) {
    fun observeAll(): LiveData<List<Subscription>> = subscriptionDao.observeAll()

    fun observeSubscriptionWithUnread(): LiveData<List<SubscriptionWithUnread>> = subscriptionDao.observeAllWithUnread()
}