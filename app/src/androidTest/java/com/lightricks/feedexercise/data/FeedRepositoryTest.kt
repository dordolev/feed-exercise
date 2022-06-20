package com.lightricks.feedexercise.data

import android.app.Application
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.lifecycle.LiveData
import androidx.lifecycle.Observer
import androidx.room.Room
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.google.common.truth.Truth.assertThat
import com.google.common.io.Resources
import com.lightricks.feedexercise.database.FeedDatabase
import com.lightricks.feedexercise.database.FeedItemEntity
import com.lightricks.feedexercise.database.getFeedDB
import com.lightricks.feedexercise.network.FeedApiService
import com.lightricks.feedexercise.network.GetFeedResponse
import com.lightricks.feedexercise.network.TemplatesMetadataItem
import com.squareup.moshi.JsonAdapter
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import io.reactivex.Single
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit

@RunWith(AndroidJUnit4::class)
class FeedRepositoryTest {
    @get: Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    private val targetContext = InstrumentationRegistry.getInstrumentation().targetContext
    private val urlString: String =
        "https://assets.swishvideoapp.com/Android/demo/catalog/thumbnails/"

    private val moshi: Moshi = Moshi.Builder()
        .add(KotlinJsonAdapterFactory())
        .build()
    private val jsonAdapter: JsonAdapter<GetFeedResponse> =
        moshi.adapter(GetFeedResponse::class.java)

    private fun testJsonStringify(): String {
        return targetContext.resources.assets.open("get_feed_response.json").bufferedReader().use {
            it.readText()
        }
    }

    private val listOfJsonFeedItems =
        jsonAdapter.fromJson(testJsonStringify())?.templatesMetadata!!.map {
            FeedItem(it.id, urlString + it.templateThumbnailURI, it.isPremium)
        }

    private val listOfJsonEntityItems =
        jsonAdapter.fromJson(testJsonStringify())?.templatesMetadata!!.map {
            FeedItemEntity(it.id, urlString + it.templateThumbnailURI, it.isPremium)
        }

    @Mock
    private lateinit var feedDatabaseMock : FeedDatabase

    @Mock
    private val feedApiServiceMock = object : FeedApiService {
        val moshi: Moshi = Moshi.Builder()
            .add(KotlinJsonAdapterFactory())
            .build()
        val jsonAdapter: JsonAdapter<GetFeedResponse> = moshi.adapter(GetFeedResponse::class.java)

        override fun getFeed(): Single<GetFeedResponse> {
            return Single.just(jsonAdapter.fromJson(testJsonStringify()))
        }
    }

    private lateinit var feedRepository: FeedRepository

    @Before
    fun initRepository() {
        feedDatabaseMock = Room.inMemoryDatabaseBuilder(targetContext, FeedDatabase::class.java).build()
        feedRepository = FeedRepository(feedApiServiceMock, feedDatabaseMock)
    }

    @Test
    fun testRefresh_isSavedToDatabase() {
        val observerTest = feedRepository.refresh().test()

        observerTest.awaitTerminalEvent()
        observerTest.assertComplete()
        observerTest.assertNoErrors()

        val feedDatabaseListOfEntities = feedDatabaseMock.feedItemDao().getAll().blockingObserve()

        assertThat(feedDatabaseListOfEntities).containsExactlyElementsIn(listOfJsonEntityItems)
    }

    @Test
    fun testRefresh_isSavedToFeedItems() {
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