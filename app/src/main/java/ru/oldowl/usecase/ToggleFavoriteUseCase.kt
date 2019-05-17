package ru.oldowl.usecase

import ru.oldowl.core.Result
import ru.oldowl.core.UseCase
import ru.oldowl.db.dao.ArticleDao
import ru.oldowl.db.dao.SyncEventDao
import ru.oldowl.db.model.Article
import ru.oldowl.db.model.SyncEvent

class ToggleFavoriteUseCase(
        private val articleDao: ArticleDao,
        private val eventDao: SyncEventDao
) : UseCase<Article, Unit>() {

    override suspend fun run(param: Article): Result<Unit> {
        param.favorite = !param.favorite
        articleDao.updateFavoriteState(param.id, param.favorite)
        eventDao.save(SyncEvent.updateFavorite(param.originalId))

        return Result.empty()
    }
}