package ru.oldowl.ui

import android.content.Context
import android.databinding.DataBindingUtil
import android.graphics.Bitmap
import android.os.Bundle
import android.support.design.widget.Snackbar
import android.support.v7.widget.Toolbar
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.webkit.WebResourceRequest
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.ProgressBar
import kotlinx.android.synthetic.main.toolbar.*
import org.koin.standalone.inject
import ru.oldowl.R
import ru.oldowl.core.Failure
import ru.oldowl.core.RefreshScreen
import ru.oldowl.core.extension.*
import ru.oldowl.core.ui.BaseActivity
import ru.oldowl.databinding.ActivityArticleBinding
import ru.oldowl.db.model.ArticleListItem
import ru.oldowl.viewmodel.ArticleViewModel
import ru.oldowl.viewmodel.ArticleViewModel.Companion.ARTICLE

class ArticleActivity : BaseActivity() {

    private val viewModel: ArticleViewModel by inject()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        DataBindingUtil.setContentView<ActivityArticleBinding>(this, R.layout.activity_article).also {
            it.viewModel = viewModel
            it.lifecycleOwner = this

            val webViewClientImpl = WebViewClientImpl(applicationContext, it.loadingProgress, it.articleWrapper).apply {
                setOnPageFinishedListener {
                    viewModel.markRead()
                }
            }

            with(it.articleContent) {
                settings.javaScriptEnabled = false
                settings.cacheMode = WebSettings.LOAD_NO_CACHE

                webViewClient = webViewClientImpl
            }

            it.setOpenInBrowser {
                browse(viewModel.url)
            }
        }

        observe(viewModel.event) {
            when (it) {
                is RefreshScreen -> invalidateOptionsMenu()
                is Failure -> showFailure(window.decorView, it)
            }
        }

        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        viewModel.setArgument(intent.extras)
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
            R.id.mark_favorite, R.id.unmark_favorite -> viewModel.toggleFavorite()

            R.id.open_in_browser -> browse(viewModel.url)

            R.id.share -> viewModel.url?.let {
                share(it)
            }

            R.id.copy_url -> viewModel.url?.let {
                copyToClipboard(it)
                Snackbar.make(window.decorView, R.string.copy_url_to_clipboard_snackbar, Snackbar.LENGTH_SHORT).show()
            }
        }

        return super.onOptionsItemSelected(item)
    }

    companion object {
        fun openArticle(context: Context?, article: ArticleListItem) {
            val bundle = Bundle()
            bundle.putParcelable(ARTICLE, article)
            context?.startActivity<ArticleActivity>(bundle)
        }
    }
}

private class WebViewClientImpl(private val context: Context,
                                private val progressBar: ProgressBar,
                                private val wrapperView: View) : WebViewClient() {

    private var onPageFinishedListener: (() -> Unit)? = null

    override fun shouldOverrideUrlLoading(view: WebView?, url: String?): Boolean {
        context.browse(url)
        return true
    }

    override fun shouldOverrideUrlLoading(view: WebView?, request: WebResourceRequest?): Boolean {
        context.browse(request?.url.toString())
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

        onPageFinishedListener?.invoke()
    }

    fun setOnPageFinishedListener(listener: () -> Unit) {
        onPageFinishedListener = listener
    }
}