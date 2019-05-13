package ru.oldowl.usecase

import ru.oldowl.core.UseCase
import ru.oldowl.db.dao.ArticleDao
import ru.oldowl.db.model.ArticleAndSubscriptionTitle
import ru.oldowl.viewmodel.ArticleListMode

class LoadArticleListUseCase(
        private val articleDao: ArticleDao
) : UseCase<Param, List<ArticleAndSubscriptionTitle>>() {

    override suspend fun run(param: Param): List<ArticleAndSubscriptionTitle> {
        val mode = param.mode
        val hideRead = param.hideRead

        return when (mode) {
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
    }
}

data class Param(
        val hideRead: Boolean,
        val mode: ArticleListMode,
        val subscriptionId: Long? = null
)