package ru.oldowl.viewmodel

import android.arch.lifecycle.MutableLiveData
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import ru.oldowl.api.feedly.FeedlyApi
import ru.oldowl.api.theoldreader.TheOldReaderApi
import ru.oldowl.core.ui.BaseViewModel
import ru.oldowl.db.dao.SubscriptionDao
import ru.oldowl.db.model.Subscription
import ru.oldowl.service.AccountService

// FIXME
class AddSubscriptionViewModel(private val feedlyApi: FeedlyApi,
                               private val accountService: AccountService,
                               private val theOldReaderApi: TheOldReaderApi,
                               private val subscriptionDao: SubscriptionDao) : BaseViewModel() {

    private val account by lazy { accountService.getAccount() }

    val searchResult: MutableLiveData<List<Subscription>> = MutableLiveData()
    val saveResult: MutableLiveData<SaveResult> = MutableLiveData()

    fun search(query: String) = launch {
        val deferred = async {
            feedlyApi.searchSubscription(query)
        }

        withContext(Dispatchers.Main) {
            searchResult.value = deferred.await()
        }
    }

    fun save(value: Subscription) = launch {
        val streamId = theOldReaderApi.addSubscription(value.url!!, account?.authToken!!)

        val success = streamId?.run {
            value.feedId = this
            subscriptionDao.save(value) > 0
        } ?: false

        withContext(Dispatchers.Main) {
            saveResult.value = SaveResult(success, value)
        }
    }

    class SaveResult(val success: Boolean,
                     val subscription: Subscription)
}