package ru.oldowl.usecase

import ru.oldowl.core.Result
import ru.oldowl.core.UseCase
import ru.oldowl.db.model.Subscription
import ru.oldowl.repository.ArticleRepository

class MarkAllReadUseCase(
        private val repository: ArticleRepository
) : UseCase<Subscription?, List<String>>() {

    override suspend fun run(param: Subscription?): Result<List<String>> =
            Result.success(
                    when (param) {
                        null -> repository.markAllRead()
                        else -> repository.markAllRead(param)
                    }
            )
}