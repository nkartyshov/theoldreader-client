package ru.oldowl.dao

import android.arch.lifecycle.LiveData
import android.arch.persistence.room.Dao
import android.arch.persistence.room.Insert
import android.arch.persistence.room.OnConflictStrategy
import android.arch.persistence.room.Query
import ru.oldowl.model.Article
import ru.oldowl.model.ArticleAndSubscriptionTitle

@Dao
interface ArticleDao {

    @Query("select a.*, s.title as subscription_title from articles a inner join subscriptions s on a.subscription_id = s.id where read = 0")
    fun observeAllUnread(): LiveData<List<ArticleAndSubscriptionTitle>>

    @Query("select a.*, s.title as subscription_title from articles a inner join subscriptions s on a.subscription_id = s.id where favorite = 1")
    fun observeFavorite(): LiveData<List<ArticleAndSubscriptionTitle>>

    @Query("select a.*, s.title as subscription_title from articles a inner join subscriptions s on a.subscription_id = s.id where subscription_id = :subscriptionId and read = 0")
    fun observeUnread(subscriptionId: Long): LiveData<List<ArticleAndSubscriptionTitle>>

    @Query("select * from articles order by publish_date desc limit 1")
    fun findLastItem(): Article

    @Query("select 1 from articles where original_id = :originalId")
    fun exists(originalId: String): Boolean

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun save(article: Article)
}