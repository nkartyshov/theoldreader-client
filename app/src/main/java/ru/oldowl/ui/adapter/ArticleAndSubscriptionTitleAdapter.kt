package ru.oldowl.ui.adapter

import android.content.Context
import android.databinding.DataBindingUtil
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.ViewGroup
import ru.oldowl.R
import ru.oldowl.databinding.ViewArticleItemBinding
import ru.oldowl.db.model.ArticleAndSubscriptionTitle

class ArticleAndSubscriptionTitleAdapter(private val context: Context?,
                                         var articles: List<ArticleAndSubscriptionTitle> = emptyList())
    : RecyclerView.Adapter<ArticleAndSubscriptionTitleHolder>() {

    private var onItemClickListener: ((article: ArticleAndSubscriptionTitle) -> Unit)? = null

    override fun getItemCount(): Int {
        return articles.size
    }

    override fun onCreateViewHolder(root: ViewGroup, viewType: Int): ArticleAndSubscriptionTitleHolder {
        val layoutInflater = LayoutInflater.from(context)
        val dataBinding: ViewArticleItemBinding = DataBindingUtil.inflate(layoutInflater, R.layout.view_article_item, root, false)

        return ArticleAndSubscriptionTitleHolder(dataBinding)
    }

    override fun onBindViewHolder(viewHolder: ArticleAndSubscriptionTitleHolder, position: Int) {
        val item = articles[position]
        viewHolder.bind(item)

        viewHolder.itemView.setOnClickListener {
            onItemClickListener?.invoke(item)
        }
    }

    fun update(items: List<ArticleAndSubscriptionTitle>) {
        articles = items
        notifyDataSetChanged()
    }

    fun setOnItemClickListener(listener: (article: ArticleAndSubscriptionTitle) -> Unit) {
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
