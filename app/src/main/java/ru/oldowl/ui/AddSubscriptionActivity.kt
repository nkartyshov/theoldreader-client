package ru.oldowl.ui

import android.os.Bundle
import android.os.PersistableBundle
import android.view.ActionMode
import android.view.Menu
import android.view.inputmethod.EditorInfo
import androidx.core.widget.doOnTextChanged
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.toolbar.*
import org.koin.android.viewmodel.ext.android.viewModel
import ru.oldowl.R
import ru.oldowl.core.UiEvent.ShowSnackbar
import ru.oldowl.core.binding.RecyclerConfig
import ru.oldowl.core.extension.hideSoftKeyboard
import ru.oldowl.core.extension.observe
import ru.oldowl.core.extension.showMessage
import ru.oldowl.core.ui.BaseActivity
import ru.oldowl.databinding.ActivityAddSubscriptionBinding
import ru.oldowl.ui.adapter.SearchSubscriptionAdapter
import ru.oldowl.viewmodel.AddSubscriptionViewModel

class AddSubscriptionActivity : BaseActivity() {

    private val viewModel: AddSubscriptionViewModel by viewModel()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val listAdapter = SearchSubscriptionAdapter()
        DataBindingUtil.setContentView<ActivityAddSubscriptionBinding>(
                this,
                R.layout.activity_add_subscription
        ).also {

            it.recyclerConfig = RecyclerConfig(listAdapter,
                    LinearLayoutManager(this, RecyclerView.VERTICAL, false),
                    DividerItemDecoration(this, LinearLayoutManager.VERTICAL),
                    it.emptyView
            )

            it.searchField.setOnEditorActionListener { v, actionId, _ ->
                if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                    hideSoftKeyboard()
                    viewModel.search(v.text.toString())
                    return@setOnEditorActionListener true
                }

                return@setOnEditorActionListener false
            }

            it.viewModel = viewModel
            it.lifecycleOwner = this
        }

        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        listAdapter.onItemClick = {
            viewModel.save(it)
        }

        observe(viewModel.searchResult) { list ->
            listAdapter.submitList(list)
        }

        observe(viewModel.event) {
            when (it) {
                is ShowSnackbar -> showMessage(window.decorView, it)
            }
        }
    }
}
