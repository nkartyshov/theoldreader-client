package ru.oldowl.usecase

import ru.oldowl.api.theoldreader.TheOldReaderApi
import ru.oldowl.core.Result
import ru.oldowl.core.UseCase
import ru.oldowl.db.dao.SubscriptionDao
import ru.oldowl.db.model.Subscription
import ru.oldowl.service.AccountService

class AddSubscriptionUseCase(
        private val accountService: AccountService,
        private val theOldReaderApi: TheOldReaderApi,
        private val subscriptionDao: SubscriptionDao
) : UseCase<Subscription, Unit>() {

    private val account by lazy { accountService.getAccount() }

    override suspend fun run(param: Subscription): Result<Unit> {
        return theOldReaderApi.addSubscription(param.url!!, account?.authToken!!)?.let {
            param.feedId = it
            subscriptionDao.save(param)
            Result.empty()
        } ?: Result.failure("Error adding subscription")
    }
}