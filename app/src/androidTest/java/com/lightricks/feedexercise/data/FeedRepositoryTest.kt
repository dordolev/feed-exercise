package com.lightricks.feedexercise.data

import androidx.lifecycle.LiveData
import androidx.lifecycle.Observer
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.runner.RunWith
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit

@RunWith(AndroidJUnit4::class)
class FeedRepositoryTest {
<<<<<<< HEAD
   //todo: add the tests here
=======
    @get: Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()
    private val fileName = "get_feed_response.json"
    private val targetContext = InstrumentationRegistry.getInstrumentation().targetContext
    private val jsonTestItems = JsonTestItems(targetContext, fileName)

    private val listOfJsonFeedItems = jsonTestItems.listOfJsonFeedItems()

    private val listOfJsonEntityItems = jsonTestItems.listOfJsonEntityItems()

    private lateinit var feedDatabaseMock : FeedDatabase

    private val feedApiServiceMock = object : FeedApiService {
        val moshi: Moshi = Moshi.Builder()
            .add(KotlinJsonAdapterFactory())
            .build()
        val jsonAdapter: JsonAdapter<GetFeedResponse> = moshi.adapter(GetFeedResponse::class.java)

        override fun getFeed(): Single<GetFeedResponse> {
            return Single.just(jsonAdapter.fromJson(jsonTestItems.testJsonStringify()))
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
>>>>>>> 7275d2d (Fix PR comments on tests)
}
