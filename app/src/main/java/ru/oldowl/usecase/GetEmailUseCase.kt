package ru.oldowl.usecase

import ru.oldowl.core.Result
import ru.oldowl.core.UseCase
import ru.oldowl.repository.AccountRepository

class GetEmailUseCase(
        private val accountRepository: AccountRepository
) : UseCase<Unit, String>() {

    override suspend fun run(param: Unit): Result<String> =
            Result.success(accountRepository.getAccountOrThrow().email)
}