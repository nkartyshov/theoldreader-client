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

    suspend fun saveOrUpdate(subscription: Subscription)

    suspend fun delete(subscription: Subscription)

    suspend fun addSubscription(subscription: Subscription): Boolean

    suspend fun unsubscribe(subscription: Subscription)

    suspend fun downloadSubscription(): List<Subscription>

    class SubscriptionRepositoryImpl(
            private val subscriptionDao: SubscriptionDao,
            private val syncEventDao: SyncEventDao,
            private val accountRepository: AccountRepository,
            private val theOldReaderApi: TheOldReaderApi
    ) : SubscriptionRepository {

        private val authToken by lazy { accountRepository.getAuthTokenOrThrow() }

        override suspend fun getNavItems(): List<SubscriptionNavItem> =
                subscriptionDao
                        .fetchNavItems()
                        .sortedByDescending { it.unread }
                        .sortedBy { it.subscription.title }

        override suspend fun findAll(): List<Subscription> = subscriptionDao.findAll()

        override suspend fun findById(id: Long): Subscription = subscriptionDao.findById(id)

        override suspend fun addSubscription(subscription: Subscription): Boolean {
            val feedId = theOldReaderApi.addSubscription(subscription.url, authToken)

            return feedId?.let {
                subscription.id = it
                subscriptionDao.save(subscription)
                true
            } ?: false
        }

        override suspend fun saveOrUpdate(subscription: Subscription) {
            if (subscriptionDao.exists(subscription.id))
                subscriptionDao.update(subscription)
            else subscriptionDao.save(subscription)
        }

        override suspend fun delete(subscription: Subscription) {
            subscriptionDao.delete(subscription)
        }

        override suspend fun unsubscribe(subscription: Subscription) {
            subscriptionDao.delete(subscription)

            if (!theOldReaderApi.unsubscribe(subscription.id, authToken)) {
                syncEventDao.save(SyncEvent.unsubscribe(subscription.id))
            }
        }

        override suspend fun downloadSubscription(): List<Subscription> =
                theOldReaderApi.getSubscriptions(authToken).map {
                    val categories = it.categories
                    val category = categories.singleOrNull()

                    Subscription(
                            categoryId = category?.id,
                            id = it.id,
                            title = it.title,
                            url = it.url,
                            htmlUrl = it.htmlUrl
                    )
                }
    }
}