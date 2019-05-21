package ru.oldowl.usecase

import ru.oldowl.core.Result
import ru.oldowl.core.UseCase
import ru.oldowl.db.dao.ArticleDao
import ru.oldowl.repository.ArticleRepository

class DeleteAllReadUseCase(
        private val repository: ArticleRepository
) : UseCase<String?, Unit>() {

    override suspend fun run(param: String?): Result<Unit> {
        if (param != null) repository.deleteAllRead(param) else repository.deleteAllRead()
        return Result.empty()
    }
}