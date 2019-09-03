package ru.oldowl.db.dao

import androidx.room.*
import ru.oldowl.db.model.Article
import ru.oldowl.db.model.ArticleListItem
import java.util.*

@Dao
interface ArticleDao {

    @Query("select a.*, s.title as subscription_title " +
            "from articles a " +
            "inner join subscriptions s on a.subscription_id = s.id " +
            "where deleted = 0 " +
            "order by a.publish_date desc")
    fun findAll(): List<ArticleListItem>

    @Query("select a.*, s.title as subscription_title " +
            "from articles a " +
            "inner join subscriptions s on a.subscription_id = s.id " +
            "where a.subscription_id = :subscriptionId " +
            "  and deleted = 0 " +
            "order by a.publish_date desc")
    fun findAll(subscriptionId: String?): List<ArticleListItem>

    @Query("select a.*, s.title as subscription_title " +
            "from articles a " +
            "inner join subscriptions s on a.subscription_id = s.id " +
            "where a.read = 0 " +
            "  and deleted = 0 " +
            "order by a.publish_date desc")
    fun findUnread(): List<ArticleListItem>

    @Query("select a.*, s.title as subscription_title " +
            "from articles a " +
            "inner join subscriptions s on a.subscription_id = s.id " +
            "where a.subscription_id = :subscriptionId " +
            "  and a.read = 0 and deleted = 0 " +
            "order by a.publish_date desc")
    fun findUnread(subscriptionId: String?): List<ArticleListItem>

    @Query("select a.*, s.title as subscription_title " +
            "from articles a " +
            "left join subscriptions s on a.subscription_id = s.id " +
            "where a.favorite = 1 " +
            "  and deleted = 0 " +
            "order by a.publish_date desc")
    fun findFavorite(): List<ArticleListItem>

    @Query("select * from articles where id = :id")
    fun findById(id: String): Article?

    @Query("select id from articles " +
            "where (:subscriptionId is null or subscription_id = :subscriptionId) " +
            "and (:read is null or read = :read)")
    fun findIds(subscriptionId: String? = null, read: Boolean? = null): List<String>

    @Query("select a.*, s.title as subscription_title " +
            "from articles a " +
            "inner join subscriptions s on a.subscription_id = s.id " +
            "where (s.title like '%' || :query || '%' " +
            "or a.title like '%' || :query || '%' " +
            "or a.description like '%' || :query || '%') " +
            "and deleted = 0 " +
            "order by a.publish_date desc")
    fun search(query: String): List<ArticleListItem>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun save(article: Article)

    @Delete
    fun delete(article: Article)

    @Query("update articles set favorite = :favorite where id = :id")
    fun updateFavoriteState(id: String, favorite: Boolean)

    @Query("update articles set read = :read where id = :id")
    fun updateReadState(id: String, read: Boolean)

    @Query("update articles set read = :read where id in (:ids)")
    fun updateReadStates(ids: List<String>, read: Boolean)

    @Query("update articles set deleted = :deleted where id in (:ids)")
    fun updateDeleteStates(ids: List<String>, deleted: Boolean)

    @Query("update articles set deleted = 1 where id in (:ids)")
    fun delete(ids: List<String>)

    @Query("delete from articles where read = :read and favorite = 0 and publish_date <= :cleanupPeriod")
    fun cleanup(cleanupPeriod: Date, read: Boolean)
}