package ru.oldowl.viewmodel

import androidx.lifecycle.MutableLiveData
import ru.oldowl.R
import ru.oldowl.core.ui.BaseViewModel
import ru.oldowl.db.model.Subscription
import ru.oldowl.usecase.AddSubscriptionUseCase
import ru.oldowl.usecase.SearchSubscriptionUseCase

class AddSubscriptionViewModel(
        private val searchSubscriptionUseCase: SearchSubscriptionUseCase,
        private val addSubscriptionUseCase: AddSubscriptionUseCase) : BaseViewModel() {

    val dataLoading = MutableLiveData<Boolean>()
    val searchResult: MutableLiveData<List<Subscription>> = MutableLiveData()

    fun search(query: String) {
        dataLoading.value = true

        searchSubscriptionUseCase(query) {
            onSuccess {
                searchResult.value = it
            }

            onFailure {
                showShortSnackbar(R.string.search_subscription_error)
            }

            onComplete {
                dataLoading.value = false
            }
        }
    }

    fun reset() {
        searchResult.value = emptyList()
    }

    fun save(value: Subscription) {
        addSubscriptionUseCase(value) {
            onSuccess {
                showShortSnackbar(R.string.add_subscription_success) {
                    args(value.title)
                }
            }
            onFailure {
                showShortSnackbar(R.string.add_subscription_error)
            }
        }
    }
}