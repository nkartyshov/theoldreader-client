package ru.oldowl.core.ui

import android.arch.lifecycle.MutableLiveData
import android.databinding.ObservableBoolean
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import ru.oldowl.db.dao.SubscriptionDao
import ru.oldowl.db.model.Account
import ru.oldowl.db.model.SubscriptionAndUnreadCount
import ru.oldowl.service.AccountService
import ru.oldowl.service.SettingsService
import ru.oldowl.viewmodel.BaseViewModel
import java.text.DateFormat

class MainViewModel(private val subscriptionDao: SubscriptionDao,
                    private val accountService: AccountService,
                    private val settingsService: SettingsService) : BaseViewModel() {

    private val account: Account? by lazy { accountService.getAccount() }
    private val dateTimeFormat = DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.SHORT)

    val email: String by lazy { account?.email ?: "" }
    val hasItems: ObservableBoolean = ObservableBoolean(false)
    val subscriptions: MutableLiveData<List<SubscriptionAndUnreadCount>> = MutableLiveData()
    val lastSyncDate: MutableLiveData<String> = MutableLiveData()

    fun updateLastSyncDate() {
        settingsService.lastSyncDate?.let {
            lastSyncDate.value = dateTimeFormat.format(settingsService.lastSyncDate)
        }
    }

    fun updateSubscriptions() = launch {
        val deferred = async {
            subscriptionDao
                    .findAllWithUnread()
                    .sortedByDescending { it.unread }
        }

        val list = deferred.await()
        withContext(Dispatchers.Main) {
            subscriptions.value = list
            hasItems.set(list.isNotEmpty())
        }
    }
}