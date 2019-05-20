package ru.oldowl.usecase

import ru.oldowl.core.UseCase
import ru.oldowl.db.dao.ArticleDao
import ru.oldowl.db.model.ArticleListItem
import ru.oldowl.viewmodel.ArticleListMode
import ru.oldowl.core.Result

class LoadArticleListUseCase(
        private val articleDao: ArticleDao
) : UseCase<LoadArticleListUseCase.Param, List<ArticleListItem>>() {

    override suspend fun run(param: Param): Result<List<ArticleListItem>> {
        val mode = param.mode
        val hideRead = param.hideRead

        val list = when (mode) {
            ArticleListMode.FAVORITE -> articleDao.findFavorite()

            ArticleListMode.ALL -> {
                if (hideRead)
                    articleDao.findUnread()
                else
                    articleDao.findAll()
            }

            ArticleListMode.SUBSCRIPTION -> {
                val subscriptionId = param.subscriptionId

                if (hideRead)
                    articleDao.findUnread(subscriptionId)
                else
                    articleDao.findAll(subscriptionId)
            }
        }

        return Result.success(list)
    }

    data class Param(
            val hideRead: Boolean,
            val mode: ArticleListMode,
            val subscriptionId: Long? = null
    )
}