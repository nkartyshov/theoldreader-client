package ru.oldowl.ui.fragment

import android.arch.lifecycle.Observer
import android.os.Bundle
import android.support.v7.widget.DividerItemDecoration
import android.support.v7.widget.LinearLayoutManager
import android.view.*
import org.koin.android.viewmodel.ext.android.viewModel
import ru.oldowl.R
import ru.oldowl.binding.RecyclerConfig
import ru.oldowl.databinding.FragmentArticleListBinding
import ru.oldowl.db.model.Subscription
import ru.oldowl.ui.ArticleActivity
import ru.oldowl.ui.adapter.ArticleAndSubscriptionTitleAdapter
import ru.oldowl.viewmodel.ArticleListMode
import ru.oldowl.viewmodel.ArticleListViewModel

class ArticleListFragment : BaseFragment() {

    private val viewModel: ArticleListViewModel by viewModel()

    init {
        setHasOptionsMenu(true)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        return inflater.inflate(R.layout.fragment_article_list, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel.mode = arguments?.getSerializable(ARTICLE_LIST_MODE) as ArticleListMode
        viewModel.subscription = arguments?.getParcelable(SUBSCRIPTION) as Subscription?

        val adapter = ArticleAndSubscriptionTitleAdapter()
        adapter.onItemClick = {
            ArticleActivity.openArticle(context, it)
        }

        val density = resources.displayMetrics.density
        val distanceToTriggerSync = 256 * density.toInt()

        FragmentArticleListBinding.bind(view).also {

            it.syncList.setDistanceToTriggerSync(distanceToTriggerSync)
            it.syncList.isEnabled = viewModel.mode != ArticleListMode.FAVORITE

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

        lifecycle.addObserver(viewModel)

        activity?.title = viewModel.title

        viewModel.articles.observe(this, Observer {
            adapter.submitList(it)
        })

        viewModel.unsubscribe.observe(this, Observer {
             fragmentManager?.popBackStackImmediate()
        })
    }

    override fun onCreateOptionsMenu(menu: Menu?, inflater: MenuInflater?) {
        super.onCreateOptionsMenu(menu, inflater)
        inflater?.inflate(R.menu.menu_articles_list, menu)

        menu?.findItem(R.id.hide_read)?.isChecked = viewModel.hideRead
        menu?.findItem(R.id.unsubscribe)?.isVisible = viewModel.subscription != null
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
        private const val ARTICLE_LIST_MODE = "article_list_mode"
        private const val SUBSCRIPTION = "subscription"

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
