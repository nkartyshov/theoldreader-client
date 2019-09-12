package ru.oldowl.viewmodel

import androidx.lifecycle.MutableLiveData
import ru.oldowl.core.UiEvent
import ru.oldowl.core.ui.BaseViewModel
import ru.oldowl.db.model.SubscriptionNavItem
import ru.oldowl.repository.SyncManager
import ru.oldowl.usecase.GetEmailUseCase
import ru.oldowl.usecase.GetLastSyncDateUseCase
import ru.oldowl.usecase.GetNavigationItemListUseCase
import ru.oldowl.usecase.LogoutUseCase

class MainViewModel(private val getNavigationItemListUseCase: GetNavigationItemListUseCase,
                    private val getEmailUseCase: GetEmailUseCase,
                    private val getLastSyncDateUseCase: GetLastSyncDateUseCase,
                    private val logoutUseCase: LogoutUseCase,
                    private val syncManager: SyncManager) : BaseViewModel() {

    val email: MutableLiveData<String> = MutableLiveData()

    val subscriptions: MutableLiveData<List<SubscriptionNavItem>> = MutableLiveData()
    val hasItems: MutableLiveData<Boolean> = MutableLiveData()
    val lastSyncDate: MutableLiveData<String> = MutableLiveData()

    fun startScheduleUpdate() = syncManager.scheduleUpdate()

    fun loadEmail() {
        getEmailUseCase(Unit) {
            onSuccess {
                email.value = it
            }

            onFailure {
                // todo log error
            }
        }
    }

    fun updateDrawer() {
        updateLastSyncDate()
        updateSubscriptions()
    }

    fun logout() {
        logoutUseCase(Unit) {
            onSuccess {
                event.value = UiEvent.CloseScreen
            }

            onFailure {
                showOopsSnackBar()
            }
        }
    }

    private fun updateLastSyncDate() {
        getLastSyncDateUseCase(Unit) {
            onSuccess {
                lastSyncDate.value = it
            }

            onFailure {
                // todo log error
            }
        }
    }

    private fun updateSubscriptions() {
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