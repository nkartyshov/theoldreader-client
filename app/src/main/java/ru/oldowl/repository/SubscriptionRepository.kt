package ru.oldowl.repository

import ru.oldowl.api.theoldreader.TheOldReaderApi
import ru.oldowl.db.dao.SubscriptionDao
import ru.oldowl.db.dao.SyncEventDao
import ru.oldowl.db.model.Subscription
import ru.oldowl.db.model.SubscriptionNavItem
import ru.oldowl.db.model.SyncEvent

interface SubscriptionRepository {

    suspend fun getNavItems(): List<SubscriptionNavItem>

    suspend fun findAll(): List<Subscription>

    suspend fun findById(id: Long): Subscription

    suspend fun save(subscription: Subscription): Boolean

    suspend fun unsubscribe(subscription: Subscription)

    class SubscriptionRepositoryImpl(
            private val subscriptionDao: SubscriptionDao,
            private val syncEventDao: SyncEventDao,
            private val accountRepository: AccountRepository,
            private val theOldReaderApi: TheOldReaderApi
    ) : SubscriptionRepository {

        private val account by lazy { accountRepository.getAccountOrThrow() }

        override suspend fun getNavItems(): List<SubscriptionNavItem> =
                subscriptionDao.fetchNavItems().sortedByDescending { it.unread }

        override suspend fun findAll(): List<Subscription> = subscriptionDao.findAll()

        override suspend fun findById(id: Long): Subscription = subscriptionDao.findById(id)

        override suspend fun save(subscription: Subscription): Boolean {
            val feedId = theOldReaderApi.addSubscription(subscription.url, account.authToken)

            return feedId?.let {
                subscription.id = it
                subscriptionDao.save(subscription)
                true
            } ?: false
        }

        override suspend fun unsubscribe(subscription: Subscription) {
            subscriptionDao.delete(subscription)

            if (!theOldReaderApi.unsubscribe(subscription.id, account.authToken)) {
                syncEventDao.save(SyncEvent.unsubscribe(subscription.id))
            }
        }
    }
}