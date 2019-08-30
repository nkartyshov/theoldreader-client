package ru.oldowl.ui.fragment

import android.os.Bundle
import android.view.*
import androidx.appcompat.widget.SearchView
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import org.koin.android.viewmodel.ext.android.sharedViewModel
import ru.oldowl.R
import ru.oldowl.core.UiEvent.CloseScreen
import ru.oldowl.core.UiEvent.ShowSnackbar
import ru.oldowl.core.binding.RecyclerConfig
import ru.oldowl.core.extension.confirmDialog
import ru.oldowl.core.extension.observe
import ru.oldowl.core.extension.showMessage
import ru.oldowl.core.ui.BaseFragment
import ru.oldowl.databinding.FragmentArticleListBinding
import ru.oldowl.db.model.Subscription
import ru.oldowl.ui.ArticleActivity
import ru.oldowl.ui.adapter.ArticleListItemAdapter
import ru.oldowl.viewmodel.ArticleListMode
import ru.oldowl.viewmodel.ArticleListViewModel
import ru.oldowl.viewmodel.ArticleListViewModel.Companion.ARTICLE_LIST_MODE
import ru.oldowl.viewmodel.ArticleListViewModel.Companion.SUBSCRIPTION

class ArticleListFragment : BaseFragment() {

    private val viewModel: ArticleListViewModel by sharedViewModel()

    init {
        setHasOptionsMenu(true)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        return inflater.inflate(R.layout.fragment_article_list, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel.setArgument(arguments)

        val adapter = ArticleListItemAdapter()
        adapter.onItemClick = {
            ArticleActivity.openArticle(context, it)
        }

        val density = resources.displayMetrics.density
        val distanceToTriggerSync = 128 * density.toInt()

        FragmentArticleListBinding.bind(view).also {

            it.syncList.setDistanceToTriggerSync(distanceToTriggerSync)
            it.syncList.setOnRefreshListener {
                viewModel.sync()
            }

            it.makeAllRead.setOnClickListener {
                viewModel.markReadAll()
            }

            it.recyclerConfig = RecyclerConfig(
                    adapter,
                    LinearLayoutManager(context),
                    DividerItemDecoration(context, DividerItemDecoration.VERTICAL),
                    it.emptyView
            )

            it.viewModel = viewModel
            it.lifecycleOwner = this

        }

        activity?.title = viewModel.title

        observe(viewModel.articles) {
            adapter.submitList(it)
        }

        observe(viewModel.event) {
            when (it) {
                is ShowSnackbar -> showMessage(view, it)
                is CloseScreen -> fragmentManager?.popBackStackImmediate()
            }
        }
    }

    override fun onResume() {
        super.onResume()
        viewModel.loadArticles()
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        inflater.inflate(R.menu.menu_articles_list, menu)
    }

    override fun onPrepareOptionsMenu(menu: Menu) {
        super.onPrepareOptionsMenu(menu)
        with(menu) {
            findItem(R.id.hide_read)?.isChecked = viewModel.hideRead
            findItem(R.id.unsubscribe)?.isVisible = viewModel.hasSubscription()

            initSearchView(findItem(R.id.search_view))
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean = when (item.itemId) {
        R.id.delete_all -> {
            confirmDialog(R.string.delete_all_dialog_message) {
                viewModel.deleteAll()
            }
            true
        }

        R.id.delete_all_read -> {
            confirmDialog(R.string.delete_all_read_dialog_message) {
                viewModel.deleteAllRead()
            }
            true
        }

        R.id.unsubscribe -> {
            confirmDialog(R.string.delete_unsubscribe_dialog_message) {
                viewModel.unsubscribe()
            }
            true
        }

        R.id.read_all -> {
            viewModel.markReadAll()
            true
        }

        R.id.sync -> {
            viewModel.sync()
            true
        }

        R.id.hide_read -> {
            item.isChecked = !item.isChecked
            viewModel.hideRead = item.isChecked
            true
        }

        else -> super.onOptionsItemSelected(item)
    }

    private fun initSearchView(menu: MenuItem?) = with(menu) {
        this?.setOnActionExpandListener(object : MenuItem.OnActionExpandListener {
            override fun onMenuItemActionExpand(item: MenuItem?): Boolean {
                return true
            }

            override fun onMenuItemActionCollapse(item: MenuItem?): Boolean {
                viewModel.loadArticles()
                return true
            }
        })

        val searchView = this?.actionView as SearchView?

        searchView?.setIconifiedByDefault(true)
        searchView?.setOnQueryTextListener(object : SearchView.OnQueryTextListener {

            override fun onQueryTextSubmit(query: String?): Boolean {
                viewModel.loadArticles(query)
                return false
            }

            override fun onQueryTextChange(query: String?): Boolean {
                if (query.isNullOrBlank()) {
                    viewModel.loadArticles()
                }
                return false
            }
        })
    }

    companion object {
        fun openAllArticles(): ArticleListFragment {
            return newFragment(ArticleListMode.ALL)
        }

        fun openFavorites(): ArticleListFragment {
            return newFragment(ArticleListMode.FAVORITE)
        }

        fun openSubscription(subscription: Subscription): ArticleListFragment {
            return newFragment(ArticleListMode.SUBSCRIPTION, subscription)
        }

        private fun newFragment(articleListMode: ArticleListMode, subscription: Subscription? = null): ArticleListFragment {
            val bundle = Bundle()
            bundle.putSerializable(ARTICLE_LIST_MODE, articleListMode)

            subscription?.let {
                bundle.putParcelable(SUBSCRIPTION, it)
            }

            val articleListFragment = ArticleListFragment()
            articleListFragment.arguments = bundle

            return articleListFragment
        }
    }
}
