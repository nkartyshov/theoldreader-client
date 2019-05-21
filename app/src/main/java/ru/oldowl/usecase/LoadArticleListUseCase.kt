package ru.oldowl.usecase

import ru.oldowl.core.Result
import ru.oldowl.core.UseCase
import ru.oldowl.db.model.ArticleListItem
import ru.oldowl.repository.ArticleRepository
import ru.oldowl.viewmodel.ArticleListMode

class LoadArticleListUseCase(
        private val repository: ArticleRepository
) : UseCase<LoadArticleListUseCase.Param, List<ArticleListItem>>() {

    override suspend fun run(param: Param): Result<List<ArticleListItem>> =
            Result.success(
                    when (param.mode) {
                        ArticleListMode.FAVORITE -> repository.findFavorite()

                        ArticleListMode.ALL -> repository.findAll()

                        ArticleListMode.SUBSCRIPTION -> repository.findBySubscription(param.subscriptionId!!)
                    }
            )

    data class Param(
            val mode: ArticleListMode,
            val subscriptionId: String? = null
    )
}