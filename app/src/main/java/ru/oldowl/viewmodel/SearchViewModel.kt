package ru.oldowl.viewmodel

import androidx.lifecycle.MutableLiveData
import ru.oldowl.core.ui.BaseViewModel
import ru.oldowl.db.model.ArticleListItem
import ru.oldowl.usecase.SearchUseCase

class SearchViewModel(
    private val searchUseCase: SearchUseCase
) : BaseViewModel() {

    val searchResult = MutableLiveData<List<ArticleListItem>>()

    fun search(query: String) {
        searchUseCase(query) {
            onSuccess {
                searchResult.value = it
            }
            onFailure {
                showOopsSnackBar()
            }
        }
    }
}