package ru.oldowl.ui.fragment

import android.arch.lifecycle.Observer
import android.os.Bundle
import android.support.v7.widget.DividerItemDecoration
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.SearchView
import android.view.*
import org.koin.standalone.inject
import ru.oldowl.R
import ru.oldowl.binding.RecyclerConfig
import ru.oldowl.databinding.FragmentAddSubscriptionBinding
import ru.oldowl.ui.adapter.SearchSubscriptionAdapter
import ru.oldowl.viewmodel.AddSubscriptionViewModel

class AddSubscriptionFragment : BaseFragment() {

    private val viewModel: AddSubscriptionViewModel by inject()

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
            it.setLifecycleOwner(this)
        }.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        activity?.title = getString(R.string.nav_add_subscription)

        viewModel.searchResult.observe(this, Observer { list -> listAdapter.submitList(list) })
    }

    override fun onCreateOptionsMenu(menu: Menu?, inflater: MenuInflater?) {
        inflater?.inflate(R.menu.menu_add_subscription, menu)
        with(menu?.findItem(R.id.search_view)) {
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
