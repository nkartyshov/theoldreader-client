package ru.oldowl.ui.fragment

import android.databinding.DataBindingUtil
import android.os.Bundle
import android.support.v7.widget.DividerItemDecoration
import android.support.v7.widget.LinearLayoutManager
import android.view.*
import kotlinx.android.synthetic.main.fragment_article_list.*
import org.koin.standalone.inject
import ru.oldowl.R
import ru.oldowl.databinding.FragmentArticleListBinding
import ru.oldowl.db.model.Subscription
import ru.oldowl.ui.ArticleActivity
import ru.oldowl.ui.adapter.ArticleAndSubscriptionTitleAdapter
import ru.oldowl.viewmodel.ArticleListMode
import ru.oldowl.viewmodel.ArticleListViewModel

class ArticleListFragment : BaseFragment() {
    private val viewModel: ArticleListViewModel by inject()

    private var articleListBinding: FragmentArticleListBinding? = null

    init {
        setHasOptionsMenu(true)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        articleListBinding = DataBindingUtil.inflate(inflater, R.layout.fragment_article_list, container, false)
        return articleListBinding?.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel.mode = arguments?.getSerializable(ARTICLE_LIST_MODE) as ArticleListMode
        viewModel.subscription = arguments?.getParcelable(SUBSCRIPTION) as Subscription?

        val adapter = ArticleAndSubscriptionTitleAdapter(context)
        adapter.setOnItemClickListener {
            ArticleActivity.openArticle(context, it)
        }

        val density = resources.displayMetrics.density
        val distanceToTriggerSync = 256 * density.toInt()

        sync_list.setDistanceToTriggerSync(distanceToTriggerSync)
        sync_list.isEnabled = viewModel.mode != ArticleListMode.FAVORITE

        article_list.layoutManager = LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)
        article_list.addItemDecoration(DividerItemDecoration(context, DividerItemDecoration.VERTICAL))
        article_list.adapter = adapter

        make_all_read.setOnClickListener {
            viewModel.markReadAll()
        }

        lifecycle.addObserver(viewModel)

        articleListBinding?.viewModel = viewModel
        articleListBinding?.lifecycleOwner = this

        activity?.title = viewModel.title
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

            // FIXME Close Fragment
            R.id.unsubscribe -> viewModel.unsubscribe()

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
