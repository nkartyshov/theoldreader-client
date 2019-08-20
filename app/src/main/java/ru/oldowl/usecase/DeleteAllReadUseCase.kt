package ru.oldowl.usecase

import ru.oldowl.core.Result
import ru.oldowl.core.UseCase
import ru.oldowl.repository.ArticleRepository

class DeleteAllReadUseCase(
        private val repository: ArticleRepository
) : UseCase<String?, List<String>>() {

    override suspend fun run(param: String?): Result<List<String>> {
        return Result.success(
                param?.let { repository.deleteAllRead(param) }
                        ?: repository.deleteAllRead()
        )
    }
}