package ru.oldowl.api

import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import ru.oldowl.model.Subscription

class TheOldReaderApiTest {
    private var theOldReaderApi: TheOldReaderApi? = null

    @Before
    fun setUp() {
        theOldReaderApi = TheOldReaderApi()
    }

    @Test
    fun testAuthentication() = runBlocking {
        val token = theOldReaderApi?.authentication("nkartyshov@adguard.com", "Test12345", "OldOwl")
        assertNotNull(token)
    }

    @Test
    fun testAuthenticationFault() = runBlocking {
        val token = theOldReaderApi?.authentication("test@fault.com", "Test12345", "OldOwl")
        assertNull(token)
    }

    @Test
    fun testGetSubscription() = runBlocking {
        val token = theOldReaderApi?.authentication("nkartyshov@adguard.com", "Test12345", "OldOwl")
        val subscriptions = theOldReaderApi?.getSubscriptions(token!!)

        assertNotNull(subscriptions)
        assertEquals(subscriptions?.size, 1)
        assertEquals(subscriptions!![0].categories.size, 1)
    }

    @Test
    fun testGetArticles() = runBlocking {
        val token = theOldReaderApi?.authentication("nkartyshov@adguard.com", "Test12345", "OldOwl")
        val subscriptions = theOldReaderApi?.getSubscriptions(token!!)

        assertNotNull(subscriptions)
        assertEquals(subscriptions?.size, 1)

        val subscriptionResponse = subscriptions!![0]

        val subscription = Subscription(
                id = 1,
                feedId = subscriptionResponse.id,
                title = "",
                url = "",
                categoryId = 0
        )

        val articles = theOldReaderApi?.getArticles(subscription, token!!)
        assertNotNull(articles)
        assertTrue(articles!!.isNotEmpty())
    }

    @After
    fun tearDown() {
        theOldReaderApi = null
    }
}