package ru.oldowl.binding

import android.databinding.BindingAdapter
import android.support.annotation.StringRes
import android.support.design.widget.TextInputLayout
import android.support.v4.widget.SwipeRefreshLayout
import android.support.v7.widget.RecyclerView
import android.view.View
import android.webkit.WebView
import android.widget.ImageView
import android.widget.TextView
import com.amulyakhare.textdrawable.TextDrawable
import com.amulyakhare.textdrawable.util.ColorGenerator
import ru.oldowl.db.model.ArticleAndSubscriptionTitle
import ru.oldowl.db.model.SubscriptionAndUnreadCount
import ru.oldowl.ui.adapter.ArticleAndSubscriptionTitleAdapter
import ru.oldowl.ui.adapter.SubscriptionAndUnreadCountAdapter
import java.text.DateFormat
import java.util.*

object BindingAdapter {

    private val HTML_TAGS_REGEX = Regex("(<.*?>|<\\/.*?>)")

    private const val MIME_TYPE = "text/html"
    private const val ENCODING = "UTF-8"

    @JvmStatic
    @BindingAdapter("text")
    fun setText(imageView: ImageView, text: String) {
        val chars = if (text.length > 1) text.substring(0, 1) else text

        val drawable = TextDrawable.builder()
                .beginConfig()
                .bold()
                .endConfig()
                .buildRound(chars.toUpperCase(), ColorGenerator.MATERIAL.getColor(chars))

        imageView.setImageDrawable(drawable)
    }

    @JvmStatic
    @BindingAdapter("subscriptionWithUnread")
    fun setItems(recyclerView: RecyclerView, items: List<SubscriptionAndUnreadCount>?) {
        items?.let {
            val adapter = recyclerView.adapter as SubscriptionAndUnreadCountAdapter

            if (adapter.subscriptions != items) {
                adapter.update(items)
            }
        }
    }

    @JvmStatic
    @BindingAdapter("articles")
    fun setArticles(recyclerView: RecyclerView, items: List<ArticleAndSubscriptionTitle>?) {
        items?.let {
            val adapter = recyclerView.adapter as ArticleAndSubscriptionTitleAdapter

            if (adapter.articles != items) {
                adapter.update(items)
            }
        }
    }

    @JvmStatic
    @BindingAdapter("app:error")
    fun setError(textInputLayout: TextInputLayout, @StringRes errorRes: Int?) {
        textInputLayout.error = null
        errorRes?.let {
            val context = textInputLayout.context
            textInputLayout.error = context.getString(errorRes)
        }
    }

    @JvmStatic
    @BindingAdapter("android:text")
    fun setDate(textView: TextView, date: Date?) {
        date?.let {
            val dateTimeInstance = DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.SHORT)
            textView.text = dateTimeInstance.format(it)
        }
    }

    @JvmStatic
    @BindingAdapter("escapeText")
    fun escapeText(textView: TextView, text: String?) {
        text?.let {
            textView.text = it.replace(HTML_TAGS_REGEX, "")
        }
    }

    @JvmStatic
    @BindingAdapter("refreshing")
    fun setRefreshing(swipeRefreshLayout: SwipeRefreshLayout, refreshing: Boolean?) {
        refreshing?.let {
            swipeRefreshLayout.isRefreshing = it
        }
    }

    @JvmStatic
    @BindingAdapter("onRefresh")
    fun setOnRefreshListener(swipeRefreshLayout: SwipeRefreshLayout, onRefreshListener: SwipeRefreshLayout.OnRefreshListener?) {
        onRefreshListener?.let {
            swipeRefreshLayout.setOnRefreshListener(onRefreshListener)
        }
    }

    @JvmStatic
    @BindingAdapter("app:html")
    fun setHtmlData(webView: WebView, html: String?) {
        html?.let {
            webView.loadDataWithBaseURL("", html, MIME_TYPE, ENCODING, null)
        }
    }

    @JvmStatic
    @BindingAdapter("config")
    fun configRecyclerView(recyclerView: RecyclerView, config: RecyclerConfig?) {
        if (config == null) {
            return
        }

        recyclerView.adapter = config.adapter
        recyclerView.layoutManager = config.layoutManager

        config.dividerItemDecoration?.let {
            recyclerView.addItemDecoration(it)
        }
    }

    @JvmStatic
    @BindingAdapter("android:onClick")
    fun setOnClickListener(view: View, runnable: Runnable?) {
        view.setOnClickListener(null)
        runnable?.let {
            view.setOnClickListener {
                runnable.run()
            }
        }
    }
}

