package ru.oldowl

import android.databinding.BindingAdapter
import android.os.Build
import android.support.v4.widget.SwipeRefreshLayout
import android.support.v7.widget.RecyclerView
import android.text.Html
import android.webkit.WebView
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import com.amulyakhare.textdrawable.TextDrawable
import com.amulyakhare.textdrawable.util.ColorGenerator
import ru.oldowl.model.ArticleAndSubscriptionTitle
import ru.oldowl.model.SubscriptionAndUnreadCount
import ru.oldowl.ui.adapter.ArticleAndSubscriptionTitleAdapter
import ru.oldowl.ui.adapter.SubscriptionAndUnreadCountAdapter
import java.text.DateFormat
import java.util.*

object BindingAdapter {

    private const val MIME_TYPE = "text/html"
    private const val ENCODING = "UTF-8"

    @JvmStatic
    @BindingAdapter("text")
    fun setText(imageView: ImageView, text: String) {
        val chars = if (text.isNotBlank()) text.substring(0, 1) else text

        val drawable = TextDrawable.builder()
                .beginConfig()
                .bold()
                .endConfig()
                .buildRound(chars, ColorGenerator.MATERIAL.getColor(chars))

        imageView.setImageDrawable(drawable)
    }

    @JvmStatic
    @BindingAdapter("subscriptionWithUnread")
    fun setItems(recyclerView: RecyclerView, items: List<SubscriptionAndUnreadCount>?) {
        items?.let {
            val adapter = recyclerView.adapter as SubscriptionAndUnreadCountAdapter
            adapter.update(items)
        }
    }

    @JvmStatic
    @BindingAdapter("articles")
    fun setArticles(recyclerView: RecyclerView, items: List<ArticleAndSubscriptionTitle>?) {
        items?.let {
            val adapter = recyclerView.adapter as ArticleAndSubscriptionTitleAdapter
            adapter.update(items)
        }
    }

    @JvmStatic
    @BindingAdapter("error")
    fun setError(editText: EditText, text: String?) {
        editText.error = text
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
    @BindingAdapter("html")
    fun setHtml(textView: TextView, html: String?) {
        html?.let {
            val html = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                Html.fromHtml(html, Html.FROM_HTML_MODE_COMPACT)
            } else {
                Html.fromHtml(html)
            }

            textView.text = html
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
    @BindingAdapter("setHtml")
    fun setHtmlData(webView: WebView, html: String?) {
        html?.let {
            webView.loadDataWithBaseURL("", html, MIME_TYPE, ENCODING, null)
        }
    }
}

