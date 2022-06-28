package com.lightricks.feedexercise.data

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.lifecycle.LiveData
import androidx.lifecycle.Observer
import androidx.room.Room
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.google.common.truth.Truth.assertThat
import com.lightricks.feedexercise.database.FeedDatabase
import com.lightricks.feedexercise.database.FeedItemEntity
import com.lightricks.feedexercise.network.FeedApiService
import com.lightricks.feedexercise.network.FeedApiServiceMock
import com.lightricks.feedexercise.network.GetFeedResponse
import com.squareup.moshi.JsonAdapter
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import io.reactivex.Single
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit

@RunWith(AndroidJUnit4::class)
class FeedRepositoryTest {
    @get: Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()
    private val fileName = "get_feed_response.json"
    private val targetContext = InstrumentationRegistry.getInstrumentation().targetContext
    private val jsonTestItems = JsonTestItems(targetContext, fileName)

    private val listOfJsonFeedItems = jsonTestItems.listOfJsonFeedItems()

    private val listOfJsonEntityItems = jsonTestItems.listOfJsonEntityItems()

    private lateinit var feedDatabaseMock: FeedDatabase

    private val feedApiServiceMock = FeedApiServiceMock(jsonTestItems)

    private lateinit var feedRepository: FeedRepository

    @Before
    fun initRepository() {
        feedDatabaseMock =
            Room.inMemoryDatabaseBuilder(targetContext, FeedDatabase::class.java).build()
        feedRepository = FeedRepository(feedApiServiceMock, feedDatabaseMock)
    }

    @Test
    fun testRefresh_isSavedToDatabase() {
        feedDatabaseMock.feedItemDao().insertList(listOfJsonEntityItems)

        val feedDatabaseFeedItems: List<FeedItem>? =
            feedDatabaseMock.feedItemDao().getAll().blockingObserve()?.toFeedItems()
        val feedRepositoryFeedItems: List<FeedItem>? = feedRepository.feedItems.blockingObserve()

        assertThat(feedDatabaseFeedItems).containsExactlyElementsIn(feedRepositoryFeedItems)
    }

    @Test
    fun test_isSavedToFeedItems() {
        val observerTest = feedRepository.refresh().test()

        observerTest.awaitTerminalEvent()
        observerTest.assertComplete()
        observerTest.assertNoErrors()

        val feedRepositoryFeedItems: List<FeedItem>? = feedRepository.feedItems.blockingObserve()

        assertThat(feedRepositoryFeedItems).containsExactlyElementsIn(listOfJsonFeedItems)
    }

    @After
    fun closeDatabase() {
        feedDatabaseMock.close()
    }
}

private fun List<FeedItemEntity>.toFeedItems(): List<FeedItem> {
    return map {
        FeedItem(it.id, it.thumbnailUrl, it.isPremium)
    }
}

private fun <T> LiveData<T>.blockingObserve(): T? {
    var value: T? = null
    val latch = CountDownLatch(1)
    val observer = object : Observer<T> {
        override fun onChanged(t: T) {
            value = t
            latch.countDown()
            removeObserver(this)
        }
    }

    observeForever(observer)
    latch.await(5, TimeUnit.SECONDS)
    return value
}
