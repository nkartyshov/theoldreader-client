package ru.oldowl.usecase

import ru.oldowl.core.Result
import ru.oldowl.core.UseCase
import ru.oldowl.db.dao.ArticleDao
import ru.oldowl.db.dao.SyncEventDao
import ru.oldowl.db.model.Article
import ru.oldowl.db.model.SyncEvent

class MarkReadUseCase(
        private val articleDao: ArticleDao,
        private val eventDao: SyncEventDao
) : UseCase<Article, Unit>() {

    override suspend fun run(param: Article): Result<Unit> {
        param.read = true
        articleDao.updateReadState(param.id, param.read)
        eventDao.save(SyncEvent.updateRead(param.originalId))

        return Result.empty()
    }
}