package ru.oldowl.usecase

import ru.oldowl.core.UseCase
import ru.oldowl.db.dao.ArticleDao

class DeleteAllUseCase(
        private val articleDao: ArticleDao
) : UseCase<Long?, Unit>() {

    override suspend fun run(param: Long?) = if (param != null) articleDao.deleteAll(param) else articleDao.deleteAll()

}