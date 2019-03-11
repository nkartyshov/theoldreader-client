package ru.oldowl.ui

import android.arch.lifecycle.Observer
import android.content.Context
import android.databinding.DataBindingUtil
import android.graphics.Bitmap
import android.os.Bundle
import android.support.design.widget.Snackbar
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.webkit.WebResourceRequest
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.ProgressBar
import kotlinx.android.synthetic.main.activity_article.*
import kotlinx.android.synthetic.main.toolbar.*
import org.jetbrains.anko.startActivity
import org.koin.standalone.inject
import ru.oldowl.R
import ru.oldowl.databinding.ActivityArticleBinding
import ru.oldowl.extension.copyToClipboard
import ru.oldowl.extension.openUrl
import ru.oldowl.model.ArticleAndSubscriptionTitle
import ru.oldowl.viewmodel.ArticleViewModel

class ArticleActivity : BaseActivity() {

    private val viewModel: ArticleViewModel by inject()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val dataBinding: ActivityArticleBinding = DataBindingUtil.setContentView(this, R.layout.activity_article)

        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        viewModel.item = intent.getSerializableExtra(ARTICLE) as ArticleAndSubscriptionTitle
        title = getString(R.string.empty)

        dataBinding.viewModel = viewModel

        with(article_content.settings) {
            javaScriptEnabled = false
            cacheMode = WebSettings.LOAD_NO_CACHE
        }

        article_content.webViewClient = WebViewClientImpl(applicationContext, loading_progress, article_wrapper)

        open_in_browser.setOnClickListener {
            openUrl()
        }

        viewModel.updateUi.observe(this, Observer {
            invalidateOptionsMenu()
        })
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_article, menu)

        menu?.run {
            menu.findItem(R.id.mark_favorite)?.isVisible = !viewModel.favorite
            menu.findItem(R.id.unmark_favorite)?.isVisible = viewModel.favorite
        }

        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        when (item?.itemId) {
            R.id.mark_favorite, R.id.unmark_favorite -> {
                viewModel.toggleFavorite()
            }

            R.id.open_in_browser -> openUrl()

            R.id.copy_url -> viewModel.url?.let {
                copyToClipboard(it)

                Snackbar.make(article_content, "The url copied to clipboard", Snackbar.LENGTH_SHORT).show()
            }
        }

        return super.onOptionsItemSelected(item)
    }

    private fun openUrl() {
        viewModel.url?.let {
            openUrl(it)
        }
    }

    companion object {
        private const val ARTICLE = "article"

        fun openArticle(context: Context?, article: ArticleAndSubscriptionTitle) {
            context?.startActivity<ArticleActivity>(ARTICLE to article)
        }
    }
}

private class WebViewClientImpl(private val context: Context,
                                private val progressBar: ProgressBar,
                                private val wrapperView: View) : WebViewClient() {

    override fun shouldOverrideUrlLoading(view: WebView?, url: String?): Boolean {
        openUrl(url)
        return true
    }

    override fun shouldOverrideUrlLoading(view: WebView?, request: WebResourceRequest?): Boolean {
        openUrl(request?.url.toString())
        return true
    }

    override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) {
        wrapperView.visibility = View.GONE
        view?.visibility = View.GONE
        progressBar.visibility = View.VISIBLE
    }

    override fun onPageFinished(view: WebView?, url: String?) {
        wrapperView.visibility = View.VISIBLE
        view?.visibility = View.VISIBLE
        progressBar.visibility = View.GONE
    }

    private fun openUrl(url: String?) {
        url?.let {
            context.openUrl(it)
        }
    }
}