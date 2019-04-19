package ru.oldowl.ui.adapter

import android.support.v7.recyclerview.extensions.ListAdapter
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.ViewGroup
import ru.oldowl.databinding.ViewArticleItemBinding
import ru.oldowl.db.model.ArticleAndSubscriptionTitle
import ru.oldowl.ui.adapter.diff.SimpleDiff

class ArticleAndSubscriptionTitleAdapter
    : ListAdapter<ArticleAndSubscriptionTitle, ArticleAndSubscriptionTitleViewHolder>(
        SimpleDiff<ArticleAndSubscriptionTitle>(
                { old, new -> old.article.id == new.article.id && old.subscriptionTitle == new.subscriptionTitle }
        )
) {

    var onItemClick: ((article: ArticleAndSubscriptionTitle) -> Unit)? = null

    override fun onCreateViewHolder(root: ViewGroup, viewType: Int): ArticleAndSubscriptionTitleViewHolder {
        val dataBinding = ViewArticleItemBinding.inflate(LayoutInflater.from(root.context), root, false)
        return ArticleAndSubscriptionTitleViewHolder(dataBinding)
    }

    override fun onBindViewHolder(viewHolder: ArticleAndSubscriptionTitleViewHolder, position: Int) {
        val item = getItem(position)
        viewHolder.bind(item)

        viewHolder.itemView.setOnClickListener {
            onItemClick?.invoke(item)
        }
    }
}

class ArticleAndSubscriptionTitleViewHolder(private val dataBinding: ViewArticleItemBinding)
    : RecyclerView.ViewHolder(dataBinding.root) {

    fun bind(item: ArticleAndSubscriptionTitle) {
        dataBinding.article = item.article
        dataBinding.subscriptionTitle = item.subscriptionTitle
    }
}
