package ru.oldowl.usecase

import ru.oldowl.core.Result
import ru.oldowl.core.UseCase
import ru.oldowl.db.dao.SubscriptionDao
import ru.oldowl.db.model.SubscriptionNavItem

class GetNavigationItemListUseCase(
        private val subscriptionDao: SubscriptionDao
) : UseCase<Unit, List<SubscriptionNavItem>>() {

    override suspend fun run(param: Unit): Result<List<SubscriptionNavItem>> =
            Result.success(subscriptionDao
                    .fetchNavItems()
                    .sortedByDescending { it.unread })

}