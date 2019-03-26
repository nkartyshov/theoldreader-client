package ru.oldowl.viewmodel

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
import java.text.DateFormat

class MainViewModel(private val subscriptionDao: SubscriptionDao,
                    private val accountService: AccountService,
                    private val settingsService: SettingsService) : BaseViewModel() {

    private val account: Account? by lazy { accountService.getAccount() }
    private val dateTimeFormat = DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.SHORT)

    val email: String by lazy { account?.email ?: "" }
    val hasItems: ObservableBoolean = ObservableBoolean(false)
    val subscriptions: MutableLiveData<List<SubscriptionAndUnreadCount>> = MutableLiveData()

    fun getLastSyncDate(): String? {
        return if (settingsService.lastSyncDate != null)
            dateTimeFormat.format(settingsService.lastSyncDate)
        else null
    }

    fun updateSubscriptions() = launch {
        val deferred = async {  subscriptionDao.findAllWithUnread() }

        withContext(Dispatchers.Main) {
            val list = deferred.await()

            subscriptions.value = list
            hasItems.set(list.isNotEmpty())
        }
    }
}