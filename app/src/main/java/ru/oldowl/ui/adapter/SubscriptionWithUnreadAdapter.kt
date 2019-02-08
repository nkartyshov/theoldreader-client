package ru.oldowl.ui.adapter

import android.content.Context
import android.databinding.DataBindingUtil
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.ViewGroup
import ru.oldowl.R
import ru.oldowl.databinding.ViewArticleItemBinding
import ru.oldowl.model.SubscriptionWithUnread

class SubscriptionWithUnreadAdapter(private val context: Context)
    : RecyclerView.Adapter<ArticleViewHolder>(), UpdatableAdapter<SubscriptionWithUnread> {
    private var subscriptions = emptyList<SubscriptionWithUnread>()

    override fun getItemCount(): Int {
        return subscriptions.size
    }

    override fun onCreateViewHolder(viewGroup: ViewGroup, viewType: Int): ArticleViewHolder {
        val layoutInflater = LayoutInflater.from(context)
        val dataBinding: ViewArticleItemBinding = DataBindingUtil.inflate(layoutInflater, R.layout.view_article_item, viewGroup, false)

        return ArticleViewHolder(dataBinding)
    }

    override fun onBindViewHolder(viewHolder: ArticleViewHolder, position: Int) {
        val subscriptionWithUnread = subscriptions[position]
        viewHolder.bind(subscriptionWithUnread)
    }

    override fun update(items: List<SubscriptionWithUnread>) {
        this.subscriptions = items
        notifyDataSetChanged()
    }
}

class ArticleViewHolder(private val dataBinding: ViewArticleItemBinding) : RecyclerView.ViewHolder(dataBinding.root) {

    fun bind(subscriptionWithUnread: SubscriptionWithUnread) {
        val subscription = subscriptionWithUnread.subscription

        dataBinding.subscription = subscription
        dataBinding.unread = subscriptionWithUnread.unread
    }
}