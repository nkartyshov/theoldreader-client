package ru.oldowl.ui.adapter

import android.content.Context
import android.databinding.DataBindingUtil
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.ViewGroup
import ru.oldowl.R
import ru.oldowl.databinding.ViewArticleItemBinding
import ru.oldowl.model.Article
import ru.oldowl.model.ArticleAndSubscriptionTitle

class ArticleAndSubscriptionTitleAdapter(private val context: Context?)
    : RecyclerView.Adapter<ArticleAndSubscriptionTitleHolder>() {

    private var article = emptyList<ArticleAndSubscriptionTitle>()
    private var onItemClickListener: ((article: Article) -> Unit)? = null

    override fun getItemCount(): Int {
        return article.size
    }

    override fun onCreateViewHolder(root: ViewGroup, viewType: Int): ArticleAndSubscriptionTitleHolder {
        val layoutInflater = LayoutInflater.from(context)
        val dataBinding: ViewArticleItemBinding = DataBindingUtil.inflate(layoutInflater, R.layout.view_article_item, root, false)

        return ArticleAndSubscriptionTitleHolder(dataBinding)
    }

    override fun onBindViewHolder(viewHolder: ArticleAndSubscriptionTitleHolder, position: Int) {
        val item = article[position]
        viewHolder.bind(item)

        viewHolder.itemView.setOnClickListener {
            onItemClickListener?.invoke(item.article)
        }
    }

    fun update(items: List<ArticleAndSubscriptionTitle>) {
        article = items
        notifyDataSetChanged()
    }

    fun setOnItemClickListener(listener: (article: Article) -> Unit) {
        this.onItemClickListener = listener
    }
}

class ArticleAndSubscriptionTitleHolder(private val dataBinding: ViewArticleItemBinding)
    : RecyclerView.ViewHolder(dataBinding.root) {

    fun bind(item: ArticleAndSubscriptionTitle) {
        dataBinding.article = item.article
        dataBinding.subscriptionTitle = item.subscriptionTitle
    }
}
