package ru.oldowl.ui.adapter

import android.content.Context
import android.support.v7.recyclerview.extensions.ListAdapter
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.ViewGroup
import ru.oldowl.R
import ru.oldowl.databinding.ViewSubscriptionItemBinding
import ru.oldowl.db.model.Subscription
import ru.oldowl.db.model.SubscriptionAndUnreadCount
import ru.oldowl.ui.adapter.diff.SimpleDiff

class SubscriptionAndUnreadCountAdapter
    : ListAdapter<SubscriptionAndUnreadCount, SubscriptionAndUnreadCountAdapter.ViewHolder>(
        SimpleDiff(
                { new, old -> new.subscription.id == old.subscription.id && new.unread == old.unread }
        )
) {

    var onItemClick: (Subscription) -> Unit = {}

    override fun onCreateViewHolder(viewGroup: ViewGroup, viewType: Int): ViewHolder {
        val dataBinding = ViewSubscriptionItemBinding.inflate(LayoutInflater.from(viewGroup.context), viewGroup, false)
        return ViewHolder(dataBinding)
    }

    override fun onBindViewHolder(viewHolder: ViewHolder, position: Int) {
        val subscriptionWithUnread = getItem(position)
        viewHolder.bind(subscriptionWithUnread)

        viewHolder.itemView.setOnClickListener {
            onItemClick.invoke(subscriptionWithUnread.subscription)
        }
    }

    class ViewHolder(
            private val dataBinding: ViewSubscriptionItemBinding
    ) : RecyclerView.ViewHolder(dataBinding.root) {

        private val context: Context = dataBinding.root.context

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
}
