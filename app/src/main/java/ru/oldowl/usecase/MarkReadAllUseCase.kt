package ru.oldowl.usecase

import ru.oldowl.core.UseCase
import ru.oldowl.db.dao.ArticleDao
import ru.oldowl.db.dao.SyncEventDao
import ru.oldowl.db.model.Subscription
import ru.oldowl.db.model.SyncEvent

// TODO Added sync from server if network is available
class MarkReadAllUseCase(
        private val articleDao: ArticleDao,
        private val syncEventDao: SyncEventDao
) : UseCase<Subscription?, Unit>() {

    override suspend fun run(param: Subscription?) {
        when (param) {
            null -> articleDao.markAllRead()
            else -> articleDao.markAllRead(param.id)
        }

        val syncEvent = SyncEvent.markAllRead(param?.feedId)
        syncEventDao.save(syncEvent)
    }
}