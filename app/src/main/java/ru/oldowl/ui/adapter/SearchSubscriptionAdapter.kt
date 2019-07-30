package ru.oldowl.ui.adapter

import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import android.view.LayoutInflater
import android.view.ViewGroup
import ru.oldowl.databinding.ViewSearchSubscriptionItemBinding
import ru.oldowl.db.model.Subscription
import ru.oldowl.core.ui.SimpleDiff

class SearchSubscriptionAdapter
    : ListAdapter<Subscription, SearchSubscriptionAdapter.ViewHolder>(
        SimpleDiff<Subscription>({ old, new -> old.id == new.id })
) {

    var onItemClick: (Subscription) -> Unit = {}

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val dataBinding = ViewSearchSubscriptionItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(dataBinding, onItemClick)
    }

    override fun onBindViewHolder(viewHolder: ViewHolder, position: Int) {
        val subscription = getItem(position)
        viewHolder.bind(subscription)
    }

    class ViewHolder(
            private val dataBinding: ViewSearchSubscriptionItemBinding,
            private val onItemClick: (Subscription) -> Unit
    ) : androidx.recyclerview.widget.RecyclerView.ViewHolder(dataBinding.root) {

        fun bind(subscription: Subscription) {
            dataBinding.subscription = subscription
            dataBinding.setOnItemClick {
                onItemClick.invoke(subscription)
            }
        }
    }
}


