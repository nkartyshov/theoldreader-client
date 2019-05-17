package ru.oldowl.viewmodel

import android.arch.lifecycle.MutableLiveData
import ru.oldowl.core.ui.BaseViewModel
import ru.oldowl.db.model.SubscriptionAndUnreadCount
import ru.oldowl.service.AccountService
import ru.oldowl.service.SettingsService
import ru.oldowl.usecase.GetNavigationItemListUseCase
import java.util.*

class MainViewModel(private val getNavigationItemListUseCase: GetNavigationItemListUseCase,
                    private val accountService: AccountService,
                    private val settingsService: SettingsService) : BaseViewModel() {

    val email: String by lazy { accountService.getAccount()?.email ?: "" }

    val hasItems: MutableLiveData<Boolean> = MutableLiveData()
    val subscriptions: MutableLiveData<List<SubscriptionAndUnreadCount>> = MutableLiveData()
    val lastSyncDate: MutableLiveData<Date> = MutableLiveData()

    fun updateLastSyncDate() {
        settingsService.lastSyncDate?.let {
            lastSyncDate.value = settingsService.lastSyncDate
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