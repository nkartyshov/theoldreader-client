package ru.oldowl.ui.adapter.diff

import android.support.v7.util.DiffUtil
import ru.oldowl.db.model.Subscription

class SubscriptionDiffCallback : DiffUtil.ItemCallback<Subscription>() {
    override fun areItemsTheSame(old: Subscription, new: Subscription): Boolean = old.id == new.id

    override fun areContentsTheSame(old: Subscription, new: Subscription): Boolean = old == new
}