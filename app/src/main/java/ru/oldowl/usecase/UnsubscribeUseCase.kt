package ru.oldowl.usecase

import ru.oldowl.core.Result
import ru.oldowl.core.UseCase
import ru.oldowl.db.dao.ArticleDao
import ru.oldowl.db.dao.SubscriptionDao
import ru.oldowl.db.dao.SyncEventDao
import ru.oldowl.db.model.Subscription
import ru.oldowl.db.model.SyncEvent
import ru.oldowl.db.model.SyncEventType

// TODO Added sync from server if network is available
class UnsubscribeUseCase(
        private val subscriptionDao: SubscriptionDao,
        private val syncEventDao: SyncEventDao
) : UseCase<Subscription, Unit>() {

    override suspend fun run(param: Subscription): Result<Unit> {
        subscriptionDao.delete(param)
        syncEventDao.save(SyncEvent.unsubscribe(param.feedId!!))

        return Result.empty()
    }
}