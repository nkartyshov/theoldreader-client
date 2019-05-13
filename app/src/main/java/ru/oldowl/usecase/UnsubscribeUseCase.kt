package ru.oldowl.usecase

import ru.oldowl.core.UseCase
import ru.oldowl.db.dao.ArticleDao
import ru.oldowl.db.dao.SyncEventDao
import ru.oldowl.db.model.Subscription

// TODO Added sync from server if network is available
class UnsubscribeUseCase(
        private val articleDao: ArticleDao,
        private val syncEventDao: SyncEventDao
) : UseCase<Subscription, Unit>() {

    override suspend fun run(param: Subscription) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

}