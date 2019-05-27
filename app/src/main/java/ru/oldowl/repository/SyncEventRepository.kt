package ru.oldowl.repository

import ru.oldowl.api.theoldreader.TheOldReaderApi
import ru.oldowl.db.dao.ArticleDao
import ru.oldowl.db.dao.SyncEventDao
import ru.oldowl.db.model.SyncEvent
import ru.oldowl.db.model.SyncEventType

interface SyncEventRepository {
    suspend fun findAll(): List<SyncEvent>

    suspend fun syncEvents()

    class SyncEventRepositoryImpl(
            private val syncEventDao: SyncEventDao,
            private val articleDao: ArticleDao,

            private val accountRepository: AccountRepository,
            private val theOldReaderApi: TheOldReaderApi
    ) : SyncEventRepository {

        override suspend fun findAll(): List<SyncEvent> = syncEventDao.findAll()

        override suspend fun syncEvents() {
            val authToken = accountRepository.getAuthTokenOrThrow()

            syncEventDao.findAll()
                    .forEach { event ->
                        when (event.eventType) {
                            SyncEventType.UPDATE_READ -> event.payload?.let {
                                val article = articleDao.findById(it)
                                theOldReaderApi.updateReadState(article!!.id, article.read, authToken)
                            }

                            SyncEventType.UPDATE_FAVORITE -> event.payload?.let {
                                val article = articleDao.findById(it)
                                theOldReaderApi.updateFavoriteState(article!!.id, article.favorite, authToken)
                            }

                            SyncEventType.MARK_ALL_READ -> {
                                theOldReaderApi.markAllRead(event.payload, authToken, event.createdDate)
                            }

                            SyncEventType.UNSUBSCRIBE -> event.payload?.let {
                                theOldReaderApi.unsubscribe(it, authToken)
                            }
                        }

                        syncEventDao.delete(event)
                    }
        }
    }
}