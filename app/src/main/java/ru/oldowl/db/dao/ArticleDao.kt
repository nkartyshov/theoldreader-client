package ru.oldowl.db.dao

import android.arch.persistence.room.*
import ru.oldowl.db.model.Article
import ru.oldowl.db.model.ArticleListItem

@Dao
interface ArticleDao {

    @Query("select a.*, s.title as subscription_title from articles a inner join subscriptions s on a.subscription_id = s.id order by a.publish_date desc")
    fun findAll(): List<ArticleListItem>

    @Query("select a.*, s.title as subscription_title from articles a inner join subscriptions s on a.subscription_id = s.id where a.subscription_id = :subscriptionId order by a.publish_date desc")
    fun findAll(subscriptionId: Long?): List<ArticleListItem>

    @Query("select a.*, s.title as subscription_title from articles a inner join subscriptions s on a.subscription_id = s.id where a.read = 0 order by a.publish_date desc")
    fun findUnread(): List<ArticleListItem>

    @Query("select a.*, s.title as subscription_title from articles a inner join subscriptions s on a.subscription_id = s.id where a.subscription_id = :subscriptionId and a.read = 0 order by a.publish_date desc")
    fun findUnread(subscriptionId: Long?): List<ArticleListItem>

    @Query("select a.*, s.title as subscription_title from articles a inner join subscriptions s on a.subscription_id = s.id where a.favorite = 1 order by a.publish_date desc")
    fun findFavorite(): List<ArticleListItem>

    @Query("select * from articles where original_id = :originalId")
    fun findByOriginalId(originalId: String): Article?

    @Query("select 1 from articles where original_id = :originalId")
    fun exists(originalId: String): Boolean

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun save(article: Article)

    @Query("update articles set favorite = :favorite where id = :id")
    fun updateFavoriteState(id: Long, favorite: Boolean)

    @Query("update articles set favorite = :favorite where original_id = :originalId")
    fun updateFavoriteState(originalId: String, favorite: Boolean)

    @Query("update articles set read = :read where id = :id")
    fun updateReadState(id: Long, read: Boolean)

    @Query("update articles set read = :read where original_id = :originalId")
    fun updateReadState(originalId: String, read: Boolean)

    @Query("update articles set read = 1")
    fun markAllRead()

    @Query("update articles set read = 1 where subscription_id = :subscriptionId")
    fun markAllRead(subscriptionId: Long?)

    @Query("delete from articles")
    fun deleteAll()

    @Query("delete from articles where subscription_id = :subscriptionId")
    fun deleteAll(subscriptionId: Long?)

    @Query("delete from articles where read = 1")
    fun deleteAllRead()

    @Query("delete from articles where read = 1 and subscription_id = :subscriptionId")
    fun deleteAllRead(subscriptionId: Long?)

    @Delete
    fun delete(article: Article)
}