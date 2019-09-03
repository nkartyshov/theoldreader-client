package ru.oldowl.usecase

import ru.oldowl.core.Result
import ru.oldowl.core.UseCase
import ru.oldowl.db.model.ArticleListItem
import ru.oldowl.repository.ArticleRepository

class SearchUseCase(
        private val articleRepository: ArticleRepository
) : UseCase<String, List<ArticleListItem>>() {

    override suspend fun run(param: String): Result<List<ArticleListItem>> =
            Result.success(articleRepository.search(param))
}