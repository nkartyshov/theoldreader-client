package ru.oldowl.ui.fragment

import android.os.Bundle
import android.support.v7.widget.DividerItemDecoration
import android.support.v7.widget.LinearLayoutManager
import android.view.*
import org.koin.android.viewmodel.ext.android.sharedViewModel
import ru.oldowl.R
import ru.oldowl.core.CloseScreen
import ru.oldowl.core.Failure
import ru.oldowl.core.ShowSnackbar
import ru.oldowl.core.binding.RecyclerConfig
import ru.oldowl.core.extension.observe
import ru.oldowl.core.extension.showFailure
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
        val distanceToTriggerSync = 256 * density.toInt()

        FragmentArticleListBinding.bind(view).also {

            it.syncList.setDistanceToTriggerSync(distanceToTriggerSync)
            it.syncList.isEnabled = !viewModel.isFavoriteMode()

            it.makeAllRead.setOnClickListener {
                viewModel.markReadAll()
            }

            it.recyclerConfig = RecyclerConfig(
                    adapter,
                    LinearLayoutManager(context),
                    DividerItemDecoration(context, DividerItemDecoration.VERTICAL)
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
                is Failure -> showFailure(view, it)
                is CloseScreen -> fragmentManager?.popBackStackImmediate()
            }
        }
    }

    override fun onResume() {
        super.onResume()
        viewModel.loadArticles()
    }

    override fun onCreateOptionsMenu(menu: Menu?, inflater: MenuInflater?) {
        super.onCreateOptionsMenu(menu, inflater)
        inflater?.inflate(R.menu.menu_articles_list, menu)

        menu?.findItem(R.id.hide_read)?.isChecked = viewModel.hideRead
        menu?.findItem(R.id.unsubscribe)?.isVisible = viewModel.hasSubscription()
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        when (item?.itemId) {
            R.id.delete_all -> viewModel.deleteAll()

            R.id.delete_all_read -> viewModel.deleteAllRead()

            R.id.unsubscribe -> {
                viewModel.unsubscribe()
            }

            R.id.read_all -> viewModel.markReadAll()

            R.id.sync -> viewModel.sync()

            R.id.hide_read -> {
                item.isChecked = !item.isChecked
                viewModel.hideRead = item.isChecked
            }
        }

        return super.onOptionsItemSelected(item)
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
