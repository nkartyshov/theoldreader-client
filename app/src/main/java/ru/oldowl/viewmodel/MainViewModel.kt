package ru.oldowl.viewmodel

import android.arch.lifecycle.MutableLiveData
import ru.oldowl.core.extension.toShortDateTime
import ru.oldowl.core.ui.BaseViewModel
import ru.oldowl.db.model.SubscriptionNavItem
import ru.oldowl.repository.AccountRepository
import ru.oldowl.repository.SettingsStorage
import ru.oldowl.repository.SyncManager
import ru.oldowl.usecase.GetNavigationItemListUseCase

class MainViewModel(private val getNavigationItemListUseCase: GetNavigationItemListUseCase,
                    private val syncManager: SyncManager,
                    private val accountRepository: AccountRepository,
                    private val settingsStorage: SettingsStorage) : BaseViewModel() {

    val email: String by lazy { accountRepository.getAccount()?.email ?: "" }

    val subscriptions: MutableLiveData<List<SubscriptionNavItem>> = MutableLiveData()
    val hasItems: MutableLiveData<Boolean> = MutableLiveData()
    val lastSyncDate: MutableLiveData<String> = MutableLiveData()

    fun startScheduleUpdate() = syncManager.scheduleUpdate()

    fun updateLastSyncDate() {
        settingsStorage.lastSyncDate?.let {
            lastSyncDate.value = it.toShortDateTime()
        }
    }

    fun updateSubscriptions() {
        getNavigationItemListUseCase(Unit) {
            onSuccess {
                subscriptions.value = it
                hasItems.value = !it.isNullOrEmpty()
            }
            onFailure {
               hasItems.value = false
            }
        }
    }
}