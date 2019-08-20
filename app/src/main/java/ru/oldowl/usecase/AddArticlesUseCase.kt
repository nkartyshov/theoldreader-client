package ru.oldowl.usecase

import ru.oldowl.core.Result
import ru.oldowl.core.UseCase
import ru.oldowl.repository.ArticleRepository

class AddArticlesUseCase(
        private val articleRepository: ArticleRepository
) : UseCase<List<String>, Unit>() {

    override suspend fun run(param: List<String>): Result<Unit> {
        // FIXME
        articleRepository.updateDeletedStates(param, false)
        return Result.empty()
    }
}