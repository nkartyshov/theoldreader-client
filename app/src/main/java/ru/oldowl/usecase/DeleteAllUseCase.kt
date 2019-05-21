package ru.oldowl.usecase

import ru.oldowl.core.Result
import ru.oldowl.core.UseCase
import ru.oldowl.db.dao.ArticleDao
import ru.oldowl.repository.ArticleRepository

class DeleteAllUseCase(
        private val repository: ArticleRepository
) : UseCase<String?, Unit>() {

    override suspend fun run(param: String?): Result<Unit> {
        if (param != null) repository.deleteAll(param) else repository.deleteAll()
        return Result.empty()
    }

}