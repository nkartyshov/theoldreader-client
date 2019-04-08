package ru.oldowl.viewmodel

import android.arch.lifecycle.MutableLiveData
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import ru.oldowl.api.feedly.FeedlyApi
import ru.oldowl.db.dao.SubscriptionDao
import ru.oldowl.db.model.Subscription

class AddSubscriptionViewModel(private val feedlyApi: FeedlyApi,
                               private val subscriptionDao: SubscriptionDao) : BaseViewModel() {

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
        val success = subscriptionDao.save(value) > 0

        withContext(Dispatchers.Main) {
            saveResult.value = SaveResult(success, value)
        }
    }

    class SaveResult(val success: Boolean,
                     val subscription: Subscription)
}