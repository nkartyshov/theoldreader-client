package ru.oldowl.db.dao

import androidx.room.*
import ru.oldowl.db.model.Article
import ru.oldowl.db.model.ArticleListItem
import java.util.*

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

    @Query("select id from articles where (:subscriptionId is null or subscription_id = :subscriptionId) and (:read is null or read = :read)")
    fun findIds(subscriptionId: String? = null, read: Boolean? = null): List<String>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun save(article: Article)

    @Query("update articles set favorite = :favorite where id = :id")
    fun updateFavoriteState(id: String, favorite: Boolean)

    @Query("update articles set read = :read where id = :id")
    fun updateReadState(id: String, read: Boolean)

    @Query("update articles set read = :read where id in (:ids)")
    fun updateReadStates(ids: List<String>, read: Boolean)

    @Query("update articles set deleted = :deleted where id in (:ids)")
    fun updateDeleteStates(ids: List<String>, deleted: Boolean)

    @Query("update articles set read = 1")
    fun markAllRead()

    @Query("update articles set read = 1 where subscription_id = :subscriptionId")
    fun markAllRead(subscriptionId: String?)

    @Query("update articles set deleted = 1")
    fun deleteAll()

    @Query("update articles set deleted = 1 where subscription_id = :subscriptionId")
    fun deleteAll(subscriptionId: String?)

    @Query("update articles set deleted = 1 where read = 1")
    fun deleteAllRead()

    @Query("update articles set deleted = 1 where read = 1 and subscription_id = :subscriptionId")
    fun deleteAllRead(subscriptionId: String?)

    @Delete
    fun delete(article: Article)

    @Query("delete from articles where read = :read and favorite = 0 and publish_date < :cleanupPeriod")
    fun cleanup(cleanupPeriod: Date, read: Boolean)
}