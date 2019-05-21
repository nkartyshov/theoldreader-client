package ru.oldowl.usecase

import ru.oldowl.core.Result
import ru.oldowl.core.UseCase
import ru.oldowl.db.dao.ArticleDao
import ru.oldowl.db.dao.SyncEventDao
import ru.oldowl.db.model.Article
import ru.oldowl.db.model.SyncEvent
import ru.oldowl.repository.ArticleRepository

class MarkReadUseCase(
        private val repository: ArticleRepository
) : UseCase<Article, Unit>() {

    override suspend fun run(param: Article): Result<Unit> {
        param.read = true
        repository.updateReadState(param)

        return Result.empty()
    }
}