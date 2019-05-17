package ru.oldowl.usecase

import ru.oldowl.core.Result
import ru.oldowl.core.UseCase
import ru.oldowl.db.dao.SubscriptionDao
import ru.oldowl.db.model.SubscriptionAndUnreadCount

class GetNavigationItemListUseCase(
        private val subscriptionDao: SubscriptionDao
) : UseCase<Unit, List<SubscriptionAndUnreadCount>>() {

    override suspend fun run(param: Unit): Result<List<SubscriptionAndUnreadCount>> =
            Result.success(
                    subscriptionDao
                            .findAllWithUnread()
                            .sortedByDescending { it.unread }
            )
}