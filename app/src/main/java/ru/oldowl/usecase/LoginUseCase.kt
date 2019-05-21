package ru.oldowl.usecase

import ru.oldowl.api.theoldreader.TheOldReaderApi
import ru.oldowl.core.Result
import ru.oldowl.core.UseCase
import ru.oldowl.repository.AccountRepository

class LoginUseCase(
        private val appName: String,
        private val theOldReaderApi: TheOldReaderApi,
        private val accountRepository: AccountRepository
) : UseCase<LoginUseCase.Param, Unit>() {

    override suspend fun run(param: Param): Result<Unit> {
        val authToken = theOldReaderApi.authentication(param.email, param.password, appName) ?:
                return Result.failure("Email or password isn't valid")

        accountRepository.saveAccount(param.email, param.password, authToken)
        return Result.empty()
    }

    data class Param(
            var email: String,
            var password: String
    )
}