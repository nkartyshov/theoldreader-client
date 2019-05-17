package ru.oldowl.usecase

import ru.oldowl.core.Result
import ru.oldowl.core.UseCase
import ru.oldowl.db.dao.ArticleDao

class DeleteAllReadUseCase(
        private val articleDao: ArticleDao
) : UseCase<Long?, Unit>() {

    override suspend fun run(param: Long?): Result<Unit> {
        if (param != null) articleDao.deleteAllRead(param) else articleDao.deleteAllRead()

        return Result.empty()
    }
}