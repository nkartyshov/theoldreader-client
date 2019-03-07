package ru.oldowl.viewmodel

import android.app.Application
import kotlinx.coroutines.async
import ru.oldowl.R
import ru.oldowl.api.TheOldReaderApi
import ru.oldowl.dao.CategoryDao
import ru.oldowl.dao.SubscriptionDao
import ru.oldowl.model.Account
import ru.oldowl.model.Category
import ru.oldowl.model.Subscription
import ru.oldowl.service.AccountService

class LoginViewModel(private val application: Application,
                     private val theOldReaderApi: TheOldReaderApi,
                     private val accountService: AccountService,
                     private val subscriptionDao: SubscriptionDao,
                     private val categoryDao: CategoryDao) : BaseViewModel() {

    suspend fun authentication(email: String, password: String): String? = async {
        val appName = application.getString(R.string.app_name)
        theOldReaderApi.authentication(email, password, appName)

        return@async theOldReaderApi.authentication(email, password, appName)
    }.await()

    fun saveAccount(email: String, password: String, authToken: String) {
        val account = Account(email, password, authToken)
        accountService.saveAccount(account)
    }

    // FIXME will move to JobService
    suspend fun downloadSubscriptions(authToken: String) = async {
        val subscriptionResponses = theOldReaderApi.getSubscriptions(authToken)
        for (subscriptionResponse in subscriptionResponses) {
            val category = subscriptionResponse.categories.map {
                Category(labelId = it.id, title = it.label)
            }.getOrElse(0) { Category(1, "default", "Default") }

            val categoryId = if (categoryDao.exists(category.labelId))
                categoryDao.findIdByLabelId(category.labelId)
            else categoryDao.save(category)

            val subscription = Subscription(
                    categoryId = categoryId,
                    title = subscriptionResponse.title,
                    feedId = subscriptionResponse.id,
                    url = subscriptionResponse.url,
                    htmlUrl = subscriptionResponse.htmlUrl
            )

            val subscriptionId = subscriptionDao.save(subscription)
            subscription.id = subscriptionId
        }
    }.await()
}