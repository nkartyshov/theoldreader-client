package ru.oldowl.ui

import android.content.Context
import android.databinding.DataBindingUtil
import android.graphics.Bitmap
import android.os.Bundle
import android.view.MenuItem
import android.view.View
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.ProgressBar
import kotlinx.android.synthetic.main.activity_article.*
import kotlinx.android.synthetic.main.toolbar.*
import org.jetbrains.anko.startActivity
import org.koin.standalone.inject
import ru.oldowl.R
import ru.oldowl.databinding.ActivityArticleBinding
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

        article_content.webViewClient = WebViewClientImpl(loading_progress)
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        when (item?.itemId) {
            android.R.id.home -> onBackPressed()
        }

        return super.onOptionsItemSelected(item)
    }

    companion object {
        private const val ARTICLE = "article"

        fun openArticle(context: Context?, article: ArticleAndSubscriptionTitle) {
            context?.startActivity<ArticleActivity>(ARTICLE to article)
        }
    }
}

private class WebViewClientImpl(private val progressBar: ProgressBar) : WebViewClient() {
    override fun shouldOverrideUrlLoading(view: WebView?, url: String?): Boolean {
        return super.shouldOverrideUrlLoading(view, url)
    }

    override fun shouldOverrideUrlLoading(view: WebView?, request: WebResourceRequest?): Boolean {
        return super.shouldOverrideUrlLoading(view, request)
    }

    override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) {
        view?.visibility = View.GONE
        progressBar.visibility = View.VISIBLE
    }

    override fun onPageFinished(view: WebView?, url: String?) {
        view?.visibility = View.VISIBLE
        progressBar.visibility = View.GONE
    }
}