package ru.oldowl.ui.fragment

import android.os.Bundle
import android.view.*
import androidx.appcompat.widget.SearchView
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import org.koin.android.viewmodel.ext.android.sharedViewModel
import ru.oldowl.R
import ru.oldowl.core.UiEvent.ShowSnackbar
import ru.oldowl.core.binding.RecyclerConfig
import ru.oldowl.core.extension.observe
import ru.oldowl.core.extension.showMessage
import ru.oldowl.core.ui.BaseFragment
import ru.oldowl.databinding.FragmentAddSubscriptionBinding
import ru.oldowl.ui.adapter.SearchSubscriptionAdapter
import ru.oldowl.viewmodel.AddSubscriptionViewModel

class AddSubscriptionFragment : BaseFragment() {

    private val viewModel: AddSubscriptionViewModel by sharedViewModel()

    private val listAdapter = SearchSubscriptionAdapter()

    init {
        setHasOptionsMenu(true)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        return FragmentAddSubscriptionBinding.inflate(inflater, container, false).also {
            it.recyclerConfig = RecyclerConfig(listAdapter,
                    LinearLayoutManager(context, RecyclerView.VERTICAL, false),
                    DividerItemDecoration(context, LinearLayoutManager.VERTICAL),
                    it.emptyView
            )

            it.viewModel = viewModel
            it.lifecycleOwner = this
        }.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        activity?.title = getString(R.string.nav_add_subscription)

        listAdapter.onItemClick = {
            viewModel.save(it)
        }

        observe(viewModel.searchResult) { list ->
            listAdapter.submitList(list)
        }

        observe(viewModel.event) {
            when (it) {
                is ShowSnackbar -> showMessage(view, it)
            }
        }
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.menu_add_subscription, menu)
    }

    override fun onPrepareOptionsMenu(menu: Menu) {
        super.onPrepareOptionsMenu(menu)
        with(menu.findItem(R.id.search_view)) {

            this?.setOnActionExpandListener(object : MenuItem.OnActionExpandListener {
                override fun onMenuItemActionExpand(item: MenuItem?): Boolean {
                    return true
                }

                override fun onMenuItemActionCollapse(item: MenuItem?): Boolean {
                    viewModel.reset()
                    activity?.onBackPressed()
                    return true
                }

            })

            val searchView = this?.actionView as SearchView?

            searchView?.setIconifiedByDefault(true)
            searchView?.setOnQueryTextListener(object : SearchView.OnQueryTextListener {

                override fun onQueryTextSubmit(query: String?): Boolean {
                    query?.let { viewModel.search(it) }
                    return false
                }

                override fun onQueryTextChange(query: String?): Boolean {
                    if (query.isNullOrBlank()) {
                        viewModel.reset()
                    }
                    return false
                }
            })

            this?.expandActionView()
        }
    }
}
