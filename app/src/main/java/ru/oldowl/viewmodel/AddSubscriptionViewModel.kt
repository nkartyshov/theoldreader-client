package ru.oldowl.viewmodel

import android.arch.lifecycle.MutableLiveData
import ru.oldowl.R
import ru.oldowl.core.AddSubscriptionSuccess
import ru.oldowl.core.Event
import ru.oldowl.core.Failure
import ru.oldowl.core.ui.BaseViewModel
import ru.oldowl.db.model.Subscription
import ru.oldowl.usecase.AddSubscriptionUseCase
import ru.oldowl.usecase.SearchSubscriptionUseCase

class AddSubscriptionViewModel(
        private val searchSubscriptionUseCase: SearchSubscriptionUseCase,
        private val addSubscriptionUseCase: AddSubscriptionUseCase) : BaseViewModel() {

    val searchResult: MutableLiveData<List<Subscription>> = MutableLiveData()

    // TODO Вынести в базовый класс
    val event: MutableLiveData<Event> = MutableLiveData()

    fun search(query: String) {
        searchSubscriptionUseCase(query) {
            onSuccess { searchResult.value = it }
            onFailure { event.value = Failure(R.string.add_subscription_unknown_error, it) }
        }
    }

    fun save(value: Subscription) {
        addSubscriptionUseCase(value) {
            onSuccess {
                event.value = AddSubscriptionSuccess(value.title)
            }
            onFailure {
                event.value = Failure(R.string.add_subscription_error, it)
            }
        }
    }
}