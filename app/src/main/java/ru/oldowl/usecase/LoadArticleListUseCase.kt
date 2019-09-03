package ru.oldowl.usecase

import ru.oldowl.core.Result
import ru.oldowl.core.UseCase
import ru.oldowl.db.model.ArticleListItem
import ru.oldowl.repository.ArticleRepository
import ru.oldowl.viewmodel.ArticleListMode

class LoadArticleListUseCase(
        private val repository: ArticleRepository
) : UseCase<LoadArticleListUseCase.Param, List<ArticleListItem>>() {

    override suspend fun run(param: Param): Result<List<ArticleListItem>> {
        val list = when (param.mode) {
            ArticleListMode.FAVORITE -> repository.findFavorite()

            ArticleListMode.ALL ->
                if (param.hideRead) repository.findUnread()
                else repository.findAll()

            ArticleListMode.SUBSCRIPTION ->
                if (param.hideRead) repository.findUnread(param.subscriptionId!!)
                else repository.findAll(param.subscriptionId!!)
        }

        return Result.success(list)
    }

    data class Param(
            val mode: ArticleListMode,
            val hideRead: Boolean,
            val subscriptionId: String? = null
    )
}