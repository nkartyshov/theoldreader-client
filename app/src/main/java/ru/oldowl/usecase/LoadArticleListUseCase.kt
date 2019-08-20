package ru.oldowl.usecase

import ru.oldowl.core.Result
import ru.oldowl.core.UseCase
import ru.oldowl.db.model.ArticleListItem
import ru.oldowl.repository.ArticleRepository
import ru.oldowl.repository.SettingsStorage
import ru.oldowl.viewmodel.ArticleListMode

class LoadArticleListUseCase(
        private val repository: ArticleRepository,
        private val settingsStorage: SettingsStorage
) : UseCase<LoadArticleListUseCase.Param, List<ArticleListItem>>() {

    override suspend fun run(param: Param): Result<List<ArticleListItem>> =
            Result.success(
                    when (param.mode) {
                        ArticleListMode.FAVORITE -> repository.findFavorite()

                        ArticleListMode.ALL ->
                            if(settingsStorage.hideRead) repository.findUnread()
                            else repository.findAll()

                        ArticleListMode.SUBSCRIPTION ->
                            if (settingsStorage.hideRead) repository.findUnread(param.subscriptionId!!)
                            else repository.findAll(param.subscriptionId!!)
                    }
            )

    data class Param(
            val mode: ArticleListMode,
            val subscriptionId: String? = null
    )
}