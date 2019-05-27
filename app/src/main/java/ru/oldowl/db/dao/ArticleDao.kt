package ru.oldowl.db.dao

import android.arch.persistence.room.*
import ru.oldowl.db.model.Article
import ru.oldowl.db.model.ArticleListItem

@Dao
interface ArticleDao {

    @Query("select a.*, s.title as subscription_title from articles a inner join subscriptions s on a.subscription_id = s.id order by a.publish_date desc")
    fun findAll(): List<ArticleListItem>

    @Query("select a.*, s.title as subscription_title from articles a inner join subscriptions s on a.subscription_id = s.id where a.subscription_id = :subscriptionId order by a.publish_date desc")
    fun findAll(subscriptionId: String?): List<ArticleListItem>

    @Query("select a.*, s.title as subscription_title from articles a inner join subscriptions s on a.subscription_id = s.id where a.read = 0 order by a.publish_date desc")
    fun findUnread(): List<ArticleListItem>

    @Query("select a.*, s.title as subscription_title from articles a inner join subscriptions s on a.subscription_id = s.id where a.subscription_id = :subscriptionId and a.read = 0 order by a.publish_date desc")
    fun findUnread(subscriptionId: String?): List<ArticleListItem>

    @Query("select a.*, s.title as subscription_title from articles a left join subscriptions s on a.subscription_id = s.id where a.favorite = 1 order by a.publish_date desc")
    fun findFavorite(): List<ArticleListItem>

    @Query("select * from articles where id = :id")
    fun findById(id: String): Article?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun save(article: Article)

    @Query("update articles set favorite = :favorite where id = :id")
    fun updateFavoriteState(id: String, favorite: Boolean)

    @Query("update articles set read = :read where id = :id")
    fun updateReadState(id: String, read: Boolean)

    @Query("update articles set read = 1")
    fun markAllRead()

    @Query("update articles set read = 1 where subscription_id = :subscriptionId")
    fun markAllRead(subscriptionId: String?)

    @Query("delete from articles")
    fun deleteAll()

    @Query("delete from articles where subscription_id = :subscriptionId")
    fun deleteAll(subscriptionId: String?)

    @Query("delete from articles where read = 1")
    fun deleteAllRead()

    @Query("delete from articles where read = 1 and subscription_id = :subscriptionId")
    fun deleteAllRead(subscriptionId: String?)

    @Delete
    fun delete(article: Article)
}