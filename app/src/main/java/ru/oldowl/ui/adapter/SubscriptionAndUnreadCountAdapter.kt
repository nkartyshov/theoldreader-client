package ru.oldowl.ui.adapter

import android.content.Context
import android.databinding.DataBindingUtil
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.ViewGroup
import ru.oldowl.R
import ru.oldowl.databinding.ViewSubscriptionItemBinding
import ru.oldowl.model.Subscription
import ru.oldowl.model.SubscriptionAndUnreadCount

class SubscriptionAndUnreadCountAdapter(private val context: Context,
                                        var subscriptions: List<SubscriptionAndUnreadCount> = emptyList())
    : RecyclerView.Adapter<SubscriptionViewHolder>() {

    private var onItemClickListener: ((Subscription) -> Unit)? = null

    override fun getItemCount(): Int {
        return subscriptions.size
    }

    override fun onCreateViewHolder(viewGroup: ViewGroup, viewType: Int): SubscriptionViewHolder {
        val layoutInflater = LayoutInflater.from(context)
        val dataBinding: ViewSubscriptionItemBinding = DataBindingUtil.inflate(layoutInflater, R.layout.view_subscription_item, viewGroup, false)

        return SubscriptionViewHolder(context, dataBinding)
    }

    override fun onBindViewHolder(viewHolder: SubscriptionViewHolder, position: Int) {
        val subscriptionWithUnread = subscriptions[position]
        viewHolder.bind(subscriptionWithUnread)

        viewHolder.itemView.setOnClickListener {
            onItemClickListener?.invoke(subscriptionWithUnread.subscription)
        }
    }

    fun update(items: List<SubscriptionAndUnreadCount>) {
        this.subscriptions = items
        notifyDataSetChanged()
    }

    fun setOnItemClickListener(onItemClickListener: (subscription: Subscription) -> Unit) {
        this.onItemClickListener = onItemClickListener
    }
}

class SubscriptionViewHolder(private val context: Context,
                             private val dataBinding: ViewSubscriptionItemBinding)
    : RecyclerView.ViewHolder(dataBinding.root) {

    fun bind(subscriptionWithUnread: SubscriptionAndUnreadCount) {
        val subscription = subscriptionWithUnread.subscription
        val unread = subscriptionWithUnread.unread

        dataBinding.subscription = subscription
        dataBinding.unread = when {
            unread in 1..98 -> unread.toString()
            unread > 99 -> context.getString(R.string.unread_more_hundred)
            else -> context.getString(R.string.empty)
        }
    }
}