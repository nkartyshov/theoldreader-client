package ru.oldowl.ui.adapter

import android.support.v7.recyclerview.extensions.ListAdapter
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.ViewGroup
import ru.oldowl.databinding.ViewSearchSubscriptionItemBinding
import ru.oldowl.db.model.Subscription
import ru.oldowl.ui.adapter.diff.SubscriptionDiffCallback

class SearchSubscriptionAdapter : ListAdapter<Subscription, SearchSubscriptionViewHolder>(SubscriptionDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SearchSubscriptionViewHolder {
        val dataBinding = ViewSearchSubscriptionItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return SearchSubscriptionViewHolder(dataBinding)
    }

    override fun onBindViewHolder(viewHolder: SearchSubscriptionViewHolder, position: Int) {
        val subscription = getItem(position)
        viewHolder.bind(subscription)
    }
}

class SearchSubscriptionViewHolder(
        private val dataBinding: ViewSearchSubscriptionItemBinding
) : RecyclerView.ViewHolder(dataBinding.root) {

    fun bind(subscription: Subscription) {
        dataBinding.subscription = subscription
    }
}

