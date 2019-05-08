package ru.oldowl.ui.fragment

import android.arch.lifecycle.Observer
import android.os.Bundle
import android.support.design.widget.Snackbar
import android.support.v7.widget.DividerItemDecoration
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.SearchView
import android.view.*
import org.koin.android.viewmodel.ext.android.viewModel
import ru.oldowl.R
import ru.oldowl.core.binding.RecyclerConfig
import ru.oldowl.core.ui.BaseFragment
import ru.oldowl.databinding.FragmentAddSubscriptionBinding
import ru.oldowl.ui.adapter.SearchSubscriptionAdapter
import ru.oldowl.viewmodel.AddSubscriptionViewModel

class AddSubscriptionFragment : BaseFragment() {

    private val viewModel: AddSubscriptionViewModel by viewModel()

    private val listAdapter = SearchSubscriptionAdapter()

    init {
        setHasOptionsMenu(true)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        return FragmentAddSubscriptionBinding.inflate(inflater, container, false).also {
            it.recyclerConfig = RecyclerConfig(listAdapter,
                    LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false),
                    DividerItemDecoration(context, LinearLayoutManager.VERTICAL)
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

        viewModel
                .searchResult
                .observe(this, Observer { list ->
                    listAdapter.submitList(list)
                })

        viewModel
                .saveResult
                .observe(this, Observer {
                    val message = when (it?.success) {
                        true -> getString(R.string.add_subscription_success, it.subscription.title)
                        false -> getString(R.string.add_subscription_error, it.subscription.title)
                        null -> getString(R.string.add_subscription_unknown_error)
                    }

                    Snackbar.make(view, message, Snackbar.LENGTH_LONG).show()
                })
    }

    override fun onCreateOptionsMenu(menu: Menu?, inflater: MenuInflater?) {
        inflater?.inflate(R.menu.menu_add_subscription, menu)
        with(menu?.findItem(R.id.search_view)) {

            this?.setOnActionExpandListener(object : MenuItem.OnActionExpandListener {
                override fun onMenuItemActionExpand(item: MenuItem?): Boolean {
                    return true
                }

                override fun onMenuItemActionCollapse(item: MenuItem?): Boolean {
                    activity?.onBackPressed()
                    return true
                }

            })

            val searchView = this?.actionView as SearchView?

            searchView?.setIconifiedByDefault(true)
            searchView?.setOnQueryTextListener(object : SearchView.OnQueryTextListener {

                override fun onQueryTextSubmit(query: String?): Boolean {
                    query?.let { viewModel.search(it) }
                    return true
                }

                override fun onQueryTextChange(query: String?): Boolean {
                    return false
                }
            })

            this?.expandActionView()
        }
    }
}
