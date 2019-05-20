package ru.oldowl.viewmodel

import android.arch.lifecycle.MutableLiveData
import android.support.design.widget.Snackbar
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import ru.oldowl.R
import ru.oldowl.api.feedly.FeedlyApi
import ru.oldowl.api.theoldreader.TheOldReaderApi
import ru.oldowl.core.*
import ru.oldowl.core.ui.BaseViewModel
import ru.oldowl.db.dao.SubscriptionDao
import ru.oldowl.db.model.Subscription
import ru.oldowl.service.AccountService
import ru.oldowl.usecase.AddSubscriptionUseCase
import ru.oldowl.usecase.SearchSubscriptionUseCase

class AddSubscriptionViewModel(
        private val searchSubscriptionUseCase: SearchSubscriptionUseCase,
        private val addSubscriptionUseCase: AddSubscriptionUseCase) : BaseViewModel() {


    val searchResult: MutableLiveData<List<Subscription>> = MutableLiveData()

    val event: MutableLiveData<Event> = MutableLiveData()

    fun search(query: String) {
        searchSubscriptionUseCase(query) {
            onSuccess { searchResult.value = it }
            onFailure { event.value = Failure(R.string.add_subscription_unknown_error, it) }
        }
    }

    fun save(value: Subscription){
       addSubscriptionUseCase(value) {
           onSuccess {
               event.value = AddSubscriptionSuccess(value.title!!)
           }
           onFailure {
               event.value = Failure(R.string.add_subscription_error, it)
           }
       }
    }

    class SaveResult(val success: Boolean,
                     val subscription: Subscription)
}