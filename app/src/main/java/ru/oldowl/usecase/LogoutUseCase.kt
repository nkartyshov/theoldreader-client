package ru.oldowl.usecase

import ru.oldowl.core.Result
import ru.oldowl.core.UseCase
import ru.oldowl.db.AppDatabase
import ru.oldowl.repository.SettingsStorage
import ru.oldowl.repository.SyncManager

class LogoutUseCase(
        private val settingsStorage: SettingsStorage,
        private val appDatabase: AppDatabase,
        private val syncManager: SyncManager
) : UseCase<Unit, Unit>() {

    override suspend fun run(param: Unit): Result<Unit> {
        settingsStorage.lastSyncDate = null
        settingsStorage.account = null
        appDatabase.clearAllTables()
        syncManager.cancelAllJobs()

        return Result.empty()
    }
}