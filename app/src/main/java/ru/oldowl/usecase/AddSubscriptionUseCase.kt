package ru.oldowl.usecase

import ru.oldowl.core.Result
import ru.oldowl.core.UseCase
import ru.oldowl.db.model.Subscription
import ru.oldowl.repository.SubscriptionRepository

class AddSubscriptionUseCase(
        private val repository: SubscriptionRepository
) : UseCase<Subscription, Unit>() {

    override suspend fun run(param: Subscription): Result<Unit> {
        return if (repository.addSubscription(param))
            Result.empty()
        else Result.failure("Error adding subscription")
    }
}