package ru.oldowl.core.binding

import android.annotation.SuppressLint
import android.view.View
import android.webkit.WebView
import android.widget.ImageView
import android.widget.TextView
import androidx.annotation.StringRes
import androidx.databinding.BindingAdapter
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.amulyakhare.textdrawable.TextDrawable
import com.amulyakhare.textdrawable.util.ColorGenerator
import com.google.android.material.textfield.TextInputLayout
import ru.oldowl.core.extension.toShortDateTime
import ru.oldowl.core.ui.EmptyDataObserver
import java.util.*

object BindingAdapter {

    private val HTML_TAGS_REGEX = Regex("(<.*?>|</.*?>)")
    private val CONTROL_CHARS_REGEX = Regex("[\\t\\n]")
    private val REMOVE_EXTRA_SPACE = Regex(" {2,}+")

    private const val MIME_TYPE = "text/html"
    private const val ENCODING = "UTF-8"

    @SuppressLint("DefaultLocale")
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
            textView.text = it.toShortDateTime()
        }
    }

    @JvmStatic
    @BindingAdapter("escapeText")
    fun escapeText(textView: TextView, text: String?) =
            text?.let {
                textView.text = it
                        .replace(HTML_TAGS_REGEX, "")
                        .replace(CONTROL_CHARS_REGEX, "")
                        .replace(REMOVE_EXTRA_SPACE, " ")
            }

    @JvmStatic
    @BindingAdapter("refreshing")
    fun setRefreshing(swipeRefreshLayout: SwipeRefreshLayout, refreshing: Boolean?) =
            refreshing?.let {
                swipeRefreshLayout.isRefreshing = it
            }

    @JvmStatic
    @BindingAdapter("enable")
    fun setEnable(swipeRefreshLayout: SwipeRefreshLayout, enable: Boolean?) {
        enable?.let {
            swipeRefreshLayout.isEnabled = it
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

        config.emptyView?.let {
            val emptyDataObserver = EmptyDataObserver(recyclerView, it)
            recyclerView.adapter?.registerAdapterDataObserver(emptyDataObserver)
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

    @JvmStatic
    @BindingAdapter("app:visibility")
    fun setVisibility(view: View, visible: Boolean = true) {
        view.visibility = if (visible) View.VISIBLE else View.GONE
    }
}

