package ru.oldowl.job

import android.app.job.JobParameters
import org.koin.standalone.inject
import ru.oldowl.db.model.Subscription
import ru.oldowl.repository.*
import timber.log.Timber
import java.util.*
import java.util.concurrent.TimeUnit

class SyncJob : CoroutineJobService() {

    private val settingsStorage: SettingsStorage by inject()
    private val notificationManager: NotificationManager by inject()

    private val syncEventRepository: SyncEventRepository by inject()
    private val categoryRepository: CategoryRepository by inject()
    private val subscriptionRepository: SubscriptionRepository by inject()
    private val articleRepository: ArticleRepository by inject()

    companion object {
        const val SUBSCRIPTION_ID = "subscription_id"
        const val FORCE = "force"
    }

    override suspend fun startJob(params: JobParameters?): JobStatus {
        try {
            val extras = params?.extras
            val subscriptionId = extras?.getLong(SUBSCRIPTION_ID) ?: -1
            val force: Boolean = extras?.getInt(FORCE) == 1

            if (!shouldUpdateSubscription(force, settingsStorage.lastSyncDate)) {
                Timber.d("Autoupdate the subscriptions has been skip, last update date ${settingsStorage.lastSyncDate}")
                return JobStatus.Success
            }

            // Sync events
            syncEventRepository.syncEvents()

            // Save new categories
            categoryRepository.downloadCategory().forEach { categoryRepository.saveOrUpdate(it) }

            // Sync subscriptions
            syncSubscriptions()

            // Downloading new articles
            val subscriptions: List<Subscription> = if (subscriptionId > 0)
                listOf(subscriptionRepository.findById(subscriptionId))
            else subscriptionRepository.findAll()

            val articles = subscriptions.flatMap { articleRepository.downloadArticles(it) }
            if (articles.isNotEmpty()) {
                articles.forEach { articleRepository.save(it) }

                if (!force) {
                    notificationManager.showNewArticles(articles.size)
                }
            }

            // Sync favorites
            syncFavorites()

            articleRepository.cleanup()

            settingsStorage.lastSyncDate = Date()

            return JobStatus.Success
        } catch (t: Throwable) {
            return JobStatus.Failure(t)
        }
    }

    private fun shouldUpdateSubscription(force: Boolean, lastSyncDate: Date?): Boolean {
        val updatePeriodTime = TimeUnit.HOURS.toMillis(settingsStorage.autoUpdatePeriod)

        return force || lastSyncDate == null || (System.currentTimeMillis() - lastSyncDate.time) > updatePeriodTime
    }

    private suspend fun syncSubscriptions() {
        val old = subscriptionRepository.findAll()
        val new = subscriptionRepository.downloadSubscription()

        // Delete subscriptions
        old.minus(new).forEach { subscriptionRepository.delete(it) }

        // Save and update subscriptions
        new.forEach {
            subscriptionRepository.saveOrUpdate(it)
        }
    }

    private suspend fun syncFavorites() {
        val old = articleRepository.findFavorite().map { it.article }
        val new = articleRepository.downloadFavorites()

        // Delete favorites
        old.minus(new).forEach {
            it.favorite = false
            articleRepository.updateState(it)
        }

        new.forEach {
            articleRepository.save(it)
        }
    }
}