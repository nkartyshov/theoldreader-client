package ru.oldowl.usecase

import ru.oldowl.core.Result
import ru.oldowl.core.UseCase
import ru.oldowl.db.dao.ArticleDao
import ru.oldowl.db.dao.SyncEventDao
import ru.oldowl.db.model.Subscription
import ru.oldowl.db.model.SyncEvent
import ru.oldowl.repository.ArticleRepository

class MarkAllReadUseCase(
        private val repository: ArticleRepository
) : UseCase<Subscription?, Unit>() {

    override suspend fun run(param: Subscription?): Result<Unit> {
        when (param) {
            null -> repository.markAllRead()
            else -> repository.markAllRead(param)
        }

        return Result.empty()
    }
}