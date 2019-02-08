package ru.oldowl

import android.databinding.BindingAdapter
import android.support.v7.widget.RecyclerView
import android.widget.ImageView
import com.amulyakhare.textdrawable.TextDrawable
import com.amulyakhare.textdrawable.util.ColorGenerator
import ru.oldowl.model.SubscriptionWithUnread
import ru.oldowl.ui.adapter.SubscriptionWithUnreadAdapter

object BindingAdapter {

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
    fun setItems(recyclerView: RecyclerView, items: List<SubscriptionWithUnread>?) {
        items?.let {
            val adapter = recyclerView.adapter as SubscriptionWithUnreadAdapter
            adapter.update(items)
        }
    }
}

