package ru.oldowl.api

import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import ru.oldowl.api.theoldreader.TheOldReaderApi

class TheOldReaderApiTest {
    private var theOldReaderApi: TheOldReaderApi? = null

    @Before
    fun setUp() {
        theOldReaderApi = TheOldReaderApi()
    }

    @After
    fun tearDown() {
        theOldReaderApi = null
    }

    @Test
    fun testAuthentication() {
        val token = theOldReaderApi?.authentication("nkartyshov@adguard.com", "Test12345", "OldOwl")
        assertNotNull(token)
    }

    @Test
    fun testAuthenticationFault() {
        val token = theOldReaderApi?.authentication("test@fault.com", "Test12345", "OldOwl")
        assertNull(token)
    }

    @Test
    fun testGetSubscription() {
        val token = theOldReaderApi?.authentication("nkartyshov@adguard.com", "Test12345", "OldOwl")
        val subscriptions = theOldReaderApi?.getSubscriptions(token!!)

        assertNotNull(subscriptions)
        assertEquals(subscriptions?.size, 1)
        assertEquals(subscriptions?.first()?.categories?.size, 1)
    }

    @Test
    fun testGetItemsRef() {
        val token = theOldReaderApi?.authentication("nkartyshov@adguard.com", "Test12345", "OldOwl")
        val subscriptions = theOldReaderApi?.getSubscriptions(token!!)
        val subscriptionResponse = subscriptions?.first()

        val itemIds = theOldReaderApi?.getItemIds(subscriptionResponse?.id!!, token!!)

        assertNotNull(itemIds)
        assertTrue(itemIds?.size!! > 0)
    }

    @Test
    fun testContents() {
        val token = theOldReaderApi?.authentication("nkartyshov@adguard.com", "Test12345", "OldOwl")
        val subscriptions = theOldReaderApi?.getSubscriptions(token!!)
        val subscriptionResponse = subscriptions?.first()

        val itemIds = theOldReaderApi?.getItemIds(subscriptionResponse?.id!!, token!!)
        val contents = theOldReaderApi?.getContents(itemIds!!, token!!)

        assertNotNull(contents)
        assertTrue(contents?.size!! > 0)
    }
}