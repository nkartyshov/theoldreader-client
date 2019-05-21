package ru.oldowl.usecase

import ru.oldowl.core.Result
import ru.oldowl.core.UseCase
import ru.oldowl.db.model.SubscriptionNavItem
import ru.oldowl.repository.SubscriptionRepository

class GetNavigationItemListUseCase(
        private val repository: SubscriptionRepository
) : UseCase<Unit, List<SubscriptionNavItem>>() {

    override suspend fun run(param: Unit): Result<List<SubscriptionNavItem>> =
            Result.success(repository.getNavItems())

}