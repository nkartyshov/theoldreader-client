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

    suspend fun findAll(subscriptionId: String): List<ArticleListItem>

    suspend fun findUnread(): List<ArticleListItem>

    suspend fun findUnread(subscriptionId: String): List<ArticleListItem>

    suspend fun save(article: Article)

    suspend fun markAllRead(): List<String>

    suspend fun markAllRead(subscription: Subscription): List<String>

    suspend fun updateState(article: Article)

    suspend fun updateReadState(article: Article)

    suspend fun updateReadStates(ids: List<String>, state: Boolean)

    suspend fun updateFavoriteState(article: Article)

    suspend fun updateDeletedStates(ids: List<String>, state: Boolean)

    suspend fun deleteAll(): List<String>

    suspend fun deleteAll(subscriptionId: String): List<String>

    suspend fun deleteAllRead(): List<String>

    suspend fun deleteAllRead(subscriptionId: String): List<String>

    suspend fun cleanup()

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

        override suspend fun findAll(): List<ArticleListItem> = articleDao.findAll()

        override suspend fun findUnread(): List<ArticleListItem> = articleDao.findUnread()

        override suspend fun findAll(subscriptionId: String): List<ArticleListItem> =
                articleDao.findAll(subscriptionId)

        override suspend fun findUnread(subscriptionId: String): List<ArticleListItem> =
                articleDao.findUnread(subscriptionId)

        override suspend fun save(article: Article) {
            articleDao.save(article)
        }

        override suspend fun markAllRead(): List<String> =
                articleDao.findIds().let {
                    articleDao.markAllRead()
                    syncEventDao.save(SyncEvent.markAllRead())
                    it
                }

        override suspend fun markAllRead(subscription: Subscription): List<String> =
                articleDao.findIds(subscriptionId = subscription.id).let {
                    articleDao.markAllRead(subscriptionId = subscription.id)
                    syncEventDao.save(SyncEvent.markAllRead(subscription.id))
                    it
                }

        override suspend fun updateState(article: Article) {
            articleDao.updateReadState(article.id, article.read)
            articleDao.updateFavoriteState(article.id, article.favorite)
        }

        override suspend fun updateReadState(article: Article) {
            articleDao.updateReadState(article.id, article.read)
            syncEventDao.save(SyncEvent.updateRead(article.id))
        }

        override suspend fun updateReadStates(ids: List<String>, state: Boolean) = articleDao.updateReadStates(ids, state)

        override suspend fun updateFavoriteState(article: Article) {
            articleDao.updateFavoriteState(article.id, article.favorite)
            syncEventDao.save(SyncEvent.updateFavorite(article.id))
        }

        override suspend fun updateDeletedStates(ids: List<String>, state: Boolean) =
                articleDao.updateDeleteStates(ids, state)

        override suspend fun deleteAll(): List<String> =
                articleDao.findIds().let {
                    articleDao.deleteAll()
                    it
                }


        override suspend fun deleteAll(subscriptionId: String): List<String> =
                articleDao.findIds(subscriptionId).let {
                    articleDao.deleteAll(subscriptionId)
                    it
                }

        override suspend fun deleteAllRead(): List<String> =
                articleDao.findIds(read = true).let {
                    articleDao.deleteAllRead()
                    it
                }

        override suspend fun deleteAllRead(subscriptionId: String): List<String> =
                articleDao.findIds(subscriptionId, read = true).let {
                    articleDao.deleteAllRead(subscriptionId)
                    it
                }

        override suspend fun cleanup() {
            val cleanupReadPeriod = settingsStorage.autoCleanupReadPeriod
            if (cleanupReadPeriod != 0L) {
                articleDao.cleanup(Date(cleanupReadPeriod), read = true)
            }

            val cleanupUnreadPeriod = settingsStorage.autoCleanupUnreadPeriod
            if (cleanupUnreadPeriod != 0L) {
                articleDao.cleanup(Date(cleanupUnreadPeriod), read = false)
            }
        }

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