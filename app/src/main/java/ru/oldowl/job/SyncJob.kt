package ru.oldowl.job

import android.app.job.JobParameters
import android.os.PersistableBundle
import org.koin.standalone.inject
import ru.oldowl.db.model.Subscription
import ru.oldowl.repository.*
import timber.log.Timber
import java.util.*
import java.util.concurrent.TimeUnit
import kotlin.system.measureTimeMillis

class SyncJob : CoroutineJobService() {

    private val settingsStorage: SettingsStorage by inject()
    private val notificationManager: NotificationManager by inject()

    private val syncEventRepository: SyncEventRepository by inject()
    private val categoryRepository: CategoryRepository by inject()
    private val subscriptionRepository: SubscriptionRepository by inject()
    private val articleRepository: ArticleRepository by inject()

    private val commands = arrayListOf(
            CleanupCommand(articleRepository),
            SyncEventCommand(syncEventRepository),
            SyncCategoriesCommand(categoryRepository),
            SyncSubscriptionsCommand(subscriptionRepository),
            SyncFavoriteCommand(articleRepository),
            DownloadArticlesCommand(subscriptionRepository, articleRepository, notificationManager)
    )

    companion object {
        const val SUBSCRIPTION_ID = "subscription_id"
        const val FORCE = "force"
    }

    override suspend fun startJob(params: JobParameters?): JobStatus {
        try {
            val extras = params?.extras
            val force: Boolean = extras?.getInt(FORCE) == 1

            if (!shouldUpdateSubscription(force, settingsStorage.lastSyncDate)) {
                Timber.d("Autoupdate the subscriptions has been skip, last update date ${settingsStorage.lastSyncDate}")
                return JobStatus.Success
            }

            commands.forEach {
                val elapsedTime = measureTimeMillis {  it.execute(extras) }
                Timber.d("${it::class} elapsed time: $elapsedTime")
            }

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
}

interface SyncCommand {
    suspend fun execute(param: PersistableBundle?)
}

class CleanupCommand(
        private val articleRepository: ArticleRepository
) : SyncCommand {

    override suspend fun execute(param: PersistableBundle?) {
        articleRepository.cleanup()
    }
}

class SyncEventCommand(
        private val syncEventRepository: SyncEventRepository
) : SyncCommand {

    override suspend fun execute(param: PersistableBundle?) {
        syncEventRepository.syncEvents()
    }

}

class SyncCategoriesCommand(
        private val categoryRepository: CategoryRepository
) : SyncCommand {

    override suspend fun execute(param: PersistableBundle?) {
        categoryRepository.downloadCategory()
                .forEach {
                    categoryRepository.saveOrUpdate(it)
                }
    }

}

class SyncSubscriptionsCommand(
        private val subscriptionRepository: SubscriptionRepository
) : SyncCommand {

    override suspend fun execute(param: PersistableBundle?) {
        val old = subscriptionRepository.findAll()
        val new = subscriptionRepository.downloadSubscription()

        // Delete subscriptions
        old.minus(new).forEach { subscriptionRepository.delete(it) }

        // Save and update subscriptions
        new.forEach {
            subscriptionRepository.saveOrUpdate(it)
        }
    }
}

class SyncFavoriteCommand(
        private val articleRepository: ArticleRepository
) : SyncCommand {

    override suspend fun execute(param: PersistableBundle?) {
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

class DownloadArticlesCommand(
        private val subscriptionRepository: SubscriptionRepository,
        private val articleRepository: ArticleRepository,
        private val notificationManager: NotificationManager
) : SyncCommand {

    override suspend fun execute(param: PersistableBundle?) {
        val subscriptionId = param?.getString(SyncJob.SUBSCRIPTION_ID)
        val force: Boolean = param?.getInt(SyncJob.FORCE) == 1

        val subscriptions: List<Subscription> = subscriptionId?.let {
            listOf(subscriptionRepository.findById(subscriptionId))
        } ?: subscriptionRepository.findAll()

        val articles = subscriptions.flatMap { articleRepository.downloadArticles(it) }
        if (articles.isNotEmpty()) {
            articles.forEach { articleRepository.save(it) }

            if (!force) {
                notificationManager.showNewArticles(articles.size)
            }
        }
    }
}
