package ru.oldowl.repository

import ru.oldowl.api.theoldreader.TheOldReaderApi
import ru.oldowl.db.dao.ArticleDao
import ru.oldowl.db.dao.SyncEventDao
import ru.oldowl.db.model.Article
import ru.oldowl.db.model.ArticleListItem
import ru.oldowl.db.model.Subscription
import ru.oldowl.db.model.SyncEvent
import java.util.*

interface ArticleRepository {

    suspend fun findFavorite(): List<ArticleListItem>

    suspend fun findAll(): List<ArticleListItem>

    suspend fun findBySubscription(subscriptionId: String): List<ArticleListItem>

    suspend fun save(article: Article)

    suspend fun markAllRead()

    suspend fun markAllRead(subscription: Subscription)

    suspend fun updateState(article: Article)

    suspend fun updateReadState(article: Article)

    suspend fun updateFavoriteState(article: Article)

    suspend fun deleteAll()

    suspend fun deleteAll(subscriptionId: String)

    suspend fun deleteAllRead()

    suspend fun deleteAllRead(subscriptionId: String)

    suspend fun downloadArticles(subscription: Subscription, newerThan: Date? = null): List<Article>

    suspend fun downloadFavorites(): List<Article>

    class ArticleRepositoryImpl(
            private val articleDao: ArticleDao,
            private val syncEventDao: SyncEventDao,
            private val theOldReaderApi: TheOldReaderApi,
            private val accountRepository: AccountRepository,
            private val settingsStorage: SettingsStorage
    ) : ArticleRepository {

        private val authToken by lazy { accountRepository.getAuthTokenOrThrow() }

        override suspend fun findFavorite(): List<ArticleListItem> = articleDao.findFavorite()

        override suspend fun findAll(): List<ArticleListItem> =
                if (settingsStorage.hideRead) articleDao.findUnread()
                else articleDao.findAll()

        override suspend fun findBySubscription(subscriptionId: String): List<ArticleListItem> =
                if (settingsStorage.hideRead) articleDao.findUnread(subscriptionId)
                else articleDao.findAll(subscriptionId)

        override suspend fun save(article: Article) {
            articleDao.save(article)
        }

        override suspend fun markAllRead() {
            articleDao.markAllRead()
            syncEventDao.save(SyncEvent.markAllRead())
        }

        override suspend fun markAllRead(subscription: Subscription) {
            articleDao.markAllRead(subscription.id)
            syncEventDao.save(SyncEvent.markAllRead(subscription.id))
        }

        override suspend fun updateState(article: Article) {
            articleDao.updateReadState(article.id, article.read)
            articleDao.updateFavoriteState(article.id, article.favorite)
        }

        override suspend fun updateReadState(article: Article) {
            articleDao.updateReadState(article.id, article.read)
            syncEventDao.save(SyncEvent.updateRead(article.id))
        }

        override suspend fun updateFavoriteState(article: Article) {
            articleDao.updateFavoriteState(article.id, article.favorite)
            syncEventDao.save(SyncEvent.updateFavorite(article.id))
        }

        override suspend fun deleteAll() = articleDao.deleteAll()

        override suspend fun deleteAll(subscriptionId: String) = articleDao.deleteAll(subscriptionId)

        override suspend fun deleteAllRead() = articleDao.deleteAllRead()

        override suspend fun deleteAllRead(subscriptionId: String) = articleDao.deleteAllRead(subscriptionId)

        override suspend fun downloadArticles(subscription: Subscription, newerThan: Date?): List<Article> {
            val date = newerThan ?: settingsStorage.lastSyncDate
            val itemIds = theOldReaderApi.getItemIds(subscription.id, authToken, newerThan = date)

            if (itemIds.isEmpty()) {
                return emptyList()
            }

            return theOldReaderApi.getContents(itemIds, authToken)
                    .map {
                        Article(
                                id = it.itemId,
                                title = it.title,
                                description = it.description,
                                subscriptionId = subscription.id,
                                url = it.link,
                                publishDate = it.publishDate
                        )
                    }
        }

        override suspend fun downloadFavorites(): List<Article> {
            val favoriteIds = theOldReaderApi.getFavoriteIds(authToken)

            return theOldReaderApi.getContents(favoriteIds, authToken)
                    .map {
                        Article(
                                id = it.itemId,
                                title = it.title,
                                description = it.description,
                                subscriptionId = it.feedId,
                                url = it.link,
                                publishDate = it.publishDate,
                                read = true,
                                favorite = true
                        )
                    }
        }
    }
}