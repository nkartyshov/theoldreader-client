package ru.oldowl.ui

import android.app.SearchManager
import android.content.Intent
import android.os.Bundle
import android.view.Menu
import androidx.appcompat.widget.SearchView
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import kotlinx.android.synthetic.main.toolbar.*
import org.koin.android.viewmodel.ext.android.viewModel
import org.koin.standalone.inject
import ru.oldowl.R
import ru.oldowl.core.binding.RecyclerConfig
import ru.oldowl.core.extension.observe
import ru.oldowl.core.ui.BaseActivity
import ru.oldowl.databinding.ActivitySearchBinding
import ru.oldowl.ui.adapter.ArticleListItemAdapter
import ru.oldowl.viewmodel.SearchViewModel


class SearchActivity : BaseActivity() {

    private val viewModel: SearchViewModel by viewModel()
    private val searchManager: SearchManager by inject()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val adapter = ArticleListItemAdapter()
        adapter.onItemClick = {
            ArticleActivity.openArticle(this, it)
        }

        DataBindingUtil.setContentView<ActivitySearchBinding>(this, R.layout.activity_search).also {
            it.recyclerConfig = RecyclerConfig(
                    adapter,
                    LinearLayoutManager(this),
                    DividerItemDecoration(this, DividerItemDecoration.VERTICAL),
                    it.emptyView
            )
            it.lifecycleOwner = this
        }

        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        observe(viewModel.searchResult) {
            adapter.submitList(it)
        }

        handleIntent(intent)
    }

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        setIntent(intent)
        intent?.let(::handleIntent)
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_search, menu)
        return true
    }

    override fun onPrepareOptionsMenu(menu: Menu?): Boolean {
        val searchView = menu?.findItem(R.id.search_view)?.actionView as SearchView?
        searchView?.setSearchableInfo(searchManager.getSearchableInfo(componentName))
        searchView?.setIconifiedByDefault(true)

        return true
    }

    private fun handleIntent(intent: Intent) {
        with(intent) {
            if (action == Intent.ACTION_SEARCH) {
                getStringExtra(SearchManager.QUERY).also { query ->
                    title = query
                    viewModel.search(query)
                }
            }
        }
    }
}