package ru.oldowl.usecase

import ru.oldowl.core.Result
import ru.oldowl.core.UseCase
import ru.oldowl.core.extension.toShortDateTime
import ru.oldowl.repository.SettingsStorage

class GetLastSyncDateUseCase(
        private val settingsStorage: SettingsStorage
): UseCase<Unit, String?>() {
    
    override suspend fun run(param: Unit): Result<String?> =
            Result.success(settingsStorage.lastSyncDate?.toShortDateTime())
}