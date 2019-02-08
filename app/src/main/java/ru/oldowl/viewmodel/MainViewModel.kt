package ru.oldowl.viewmodel

import android.arch.lifecycle.LiveData
import android.databinding.ObservableBoolean
import ru.oldowl.model.Account
import ru.oldowl.model.SubscriptionWithUnread
import ru.oldowl.repository.SubscriptionRepository
import ru.oldowl.service.AccountService
import ru.oldowl.service.SettingsService
import java.text.DateFormat

class MainViewModel(private val subscriptionRepository: SubscriptionRepository,
                    private val accountService: AccountService,
                    private val settingsService: SettingsService) : BaseViewModel() {

    private val account: Account? by lazy { accountService.getAccount() }
    private val dateTimeFormat = DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.SHORT)

    val email: String by lazy { account?.email ?: "" }


    val hasItems: ObservableBoolean = ObservableBoolean(false)

    fun getLastSyncDate(): String? {
        return if (settingsService.lastSyncDate != null)
            dateTimeFormat.format(settingsService.lastSyncDate)
        else null
    }

    fun getSubscriptions(): LiveData<List<SubscriptionWithUnread>> = subscriptionRepository.observeSubscriptionWithUnread()
}