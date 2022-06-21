package com.lightricks.feedexercise.data

/**
 * This is our data layer abstraction. Users of this class don't need to know
 * where the data actually comes from (network, database or somewhere else).
 */
<<<<<<< HEAD
class FeedRepository {
    //todo: implement
}
=======
class FeedRepository(
    private val feedApiService: FeedApiService,
    private val feedDatabase: FeedDatabase
) {
    val feedItems: LiveData<List<FeedItem>> =
        Transformations.map(feedDatabase.feedItemDao().getAll()) {
            it.toFeedItems()
        }

    private var numOfRefresh: Int = -1


    private val urlString: String =
        "https://assets.swishvideoapp.com/Android/demo/catalog/thumbnails/"

    fun refresh(): Completable {
        numOfRefresh += 1
        return feedApiService.getFeed()
            .flatMapCompletable { response: GetFeedResponse ->
                feedDatabase.feedItemDao().insertList(createEntityList(response.templatesMetadata))
            }
            .subscribeOn(Schedulers.io())
    }

    fun createEntityList(list: List<TemplatesMetadataItem>): List<FeedItemEntity> {
        return list.map {
            FeedItemEntity(
                it.id,
                urlString + it.templateThumbnailURI,
                it.isPremium
            )
        }
    }

    private fun switchLast(list: List<FeedItem>): List<FeedItem> {
        if (list.isNotEmpty()) {
            val numOfRefreshMod: Int = numOfRefresh % list.size
            if (numOfRefreshMod >= 1) {
                val lastElementList = listOf(list[list.size - 1 - numOfRefreshMod])
                var newList = list.subList(
                    0,
                    list.size - 1 - numOfRefreshMod
                ) + list.subList(list.size - numOfRefreshMod, list.size - 1)
                newList = lastElementList + newList
                return newList
            } else if (numOfRefreshMod == 0) {
                val lastElementList = listOf(list[list.size - 1])
                var newList = list.subList(0, list.size - 1)
                newList = lastElementList + newList
                return newList
            }
            else {
                return list
            }
        } else {
            return list
        }
    }


    fun List<FeedItemEntity>.toSwitchedFeedItems(): List<FeedItem> {
        return switchLast(map {
            FeedItem(it.id, it.thumbnailUrl, it.isPremium)
        })
    }

    fun List<FeedItemEntity>.toFeedItems(): List<FeedItem> {
        return map {
            FeedItem(it.id, it.thumbnailUrl, it.isPremium)
        }
    }
}
>>>>>>> 7275d2d (Fix PR comments on tests)
