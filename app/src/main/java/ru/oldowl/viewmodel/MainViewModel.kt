package ru.oldowl.viewmodel

import android.arch.lifecycle.MutableLiveData
import ru.oldowl.core.extension.toShortDateTime
import ru.oldowl.core.ui.BaseViewModel
import ru.oldowl.db.model.SubscriptionNavItem
import ru.oldowl.service.AccountService
import ru.oldowl.service.SettingsService
import ru.oldowl.usecase.GetNavigationItemListUseCase

class MainViewModel(private val getNavigationItemListUseCase: GetNavigationItemListUseCase,
                    private val accountService: AccountService,
                    private val settingsService: SettingsService) : BaseViewModel() {

    val email: String by lazy { accountService.getAccount()?.email ?: "" }

    val subscriptions: MutableLiveData<List<SubscriptionNavItem>> = MutableLiveData()
    val hasItems: MutableLiveData<Boolean> = MutableLiveData()
    val lastSyncDate: MutableLiveData<String> = MutableLiveData()

    fun updateLastSyncDate() {
        settingsService.lastSyncDate?.let {
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