package ru.oldowl.ui.adapter

import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import android.view.LayoutInflater
import android.view.ViewGroup
import ru.oldowl.databinding.ViewArticleItemBinding
import ru.oldowl.db.model.ArticleListItem
import ru.oldowl.core.ui.SimpleDiff

class ArticleListItemAdapter
    : ListAdapter<ArticleListItem, ArticleListItemAdapter.ViewHolder>(
        SimpleDiff<ArticleListItem>(
                { old, new -> old == new }
        )
) {

    var onItemClick: (article: ArticleListItem) -> Unit = {}

    override fun onCreateViewHolder(root: ViewGroup, viewType: Int): ViewHolder {
        val dataBinding = ViewArticleItemBinding.inflate(LayoutInflater.from(root.context), root, false)
        return ViewHolder(dataBinding)
    }

    override fun onBindViewHolder(viewHolder: ViewHolder, position: Int) {
        val item = getItem(position)
        viewHolder.bind(item)

        viewHolder.itemView.setOnClickListener {
            onItemClick.invoke(item)
        }
    }

    class ViewHolder(private val dataBinding: ViewArticleItemBinding)
        : androidx.recyclerview.widget.RecyclerView.ViewHolder(dataBinding.root) {

        fun bind(item: ArticleListItem) {
            dataBinding.article = item.article
            dataBinding.subscriptionTitle = item.subscriptionTitle
        }
    }
}


