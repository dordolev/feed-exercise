package com.lightricks.feedexercise.data

import androidx.lifecycle.LiveData
import androidx.lifecycle.LiveDataReactiveStreams
import androidx.lifecycle.Transformations
import com.lightricks.feedexercise.database.FeedDatabase
import com.lightricks.feedexercise.database.FeedItemDao
import com.lightricks.feedexercise.database.FeedItemEntity
import com.lightricks.feedexercise.network.FeedApiService
import com.lightricks.feedexercise.network.FeedApiServiceImpl
import com.lightricks.feedexercise.network.GetFeedResponse
import io.reactivex.Completable
import io.reactivex.Scheduler
import io.reactivex.Single
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers

/**
 * This is our data layer abstraction. Users of this class don't need to know
 * where the data actually comes from (network, database or somewhere else).
 */
class FeedRepository(
    private val feedApiService: FeedApiService,
    private val feedDatabase: FeedDatabase
) {
    val feedItems: LiveData<List<FeedItem>> =
        Transformations.map(feedDatabase.feedItemDao().getAll()) {
            it.toFeedItems()
        }

    private val urlString: String =
        "https://assets.swishvideoapp.com/Android/demo/catalog/thumbnails/"

    fun refresh(): Completable {
        return feedApiService.getFeed()
            .flatMapCompletable { response: GetFeedResponse ->
                feedDatabase.feedItemDao().insertList(response.templatesMetadata.map {
                    FeedItemEntity(
                        it.id,
                        urlString + it.templateThumbnailURI,
                        it.isPremium
                    )
                })
            }
            .subscribeOn(Schedulers.io())
    }
}

fun List<FeedItemEntity>.toFeedItems(): List<FeedItem> {
    return map {
        FeedItem(it.id, it.thumbnailUrl, it.isPremium)
    }
}
