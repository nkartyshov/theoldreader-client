package ru.oldowl.repository

import ru.oldowl.db.dao.ArticleDao
import ru.oldowl.db.dao.SyncEventDao
import ru.oldowl.db.model.Article
import ru.oldowl.db.model.ArticleListItem
import ru.oldowl.db.model.Subscription
import ru.oldowl.db.model.SyncEvent

interface ArticleRepository {

    suspend fun findFavorite(): List<ArticleListItem>

    suspend fun findAll(): List<ArticleListItem>

    suspend fun findBySubscription(subscriptionId: String): List<ArticleListItem>

    suspend fun markAllRead()

    suspend fun markAllRead(subscription: Subscription)

    suspend fun updateReadState(article: Article)

    suspend fun updateFavoriteState(article: Article)

    suspend fun deleteAll()

    suspend fun deleteAll(subscriptionId: String)

    suspend fun deleteAllRead()

    suspend fun deleteAllRead(subscriptionId: String)

    class ArticleRepositoryImpl(
            private val articleDao: ArticleDao,
            private val syncEventDao: SyncEventDao,
            private val settingsStorage: SettingsStorage
    ) : ArticleRepository {

        override suspend fun findFavorite(): List<ArticleListItem> = articleDao.findFavorite()

        override suspend fun findAll(): List<ArticleListItem> =
                if (settingsStorage.hideRead) articleDao.findUnread()
                else articleDao.findAll()

        override suspend fun findBySubscription(subscriptionId: String): List<ArticleListItem> =
                if (settingsStorage.hideRead) articleDao.findUnread(subscriptionId)
                else articleDao.findAll(subscriptionId)

        override suspend fun markAllRead() {
            articleDao.markAllRead()
            syncEventDao.save(SyncEvent.markAllRead())
        }

        override suspend fun markAllRead(subscription: Subscription) {
            articleDao.markAllRead(subscription.id)
            syncEventDao.save(SyncEvent.markAllRead(subscription.id))
        }

        override suspend fun updateReadState(article: Article) {
            articleDao.updateReadState(article.id, article.read)
            syncEventDao.save(SyncEvent.updateRead(article.id))
        }

        override suspend fun updateFavoriteState(article: Article) {
            articleDao.updateReadState(article.id, article.favorite)
            syncEventDao.save(SyncEvent.updateFavorite(article.id))
        }

        override suspend fun deleteAll() = articleDao.deleteAll()

        override suspend fun deleteAll(subscriptionId: String) = articleDao.deleteAll(subscriptionId)

        override suspend fun deleteAllRead() = articleDao.deleteAllRead()

        override suspend fun deleteAllRead(subscriptionId: String) = articleDao.deleteAllRead(subscriptionId)
    }
}