package com.lightricks.feedexercise.data

import android.content.Context
import com.lightricks.feedexercise.database.FeedItemEntity
import com.lightricks.feedexercise.network.GetFeedResponse
import com.squareup.moshi.JsonAdapter
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import com.lightricks.feedexercise.data.FeedRepository
import com.lightricks.feedexercise.network.TemplatesMetadataItem

class JsonTestItems(targetContext: Context, fileName: String) {
    private val targetContext = targetContext
    private val fileName = fileName

    private val urlString: String =
        "https://assets.swishvideoapp.com/Android/demo/catalog/thumbnails/"

    private val moshi: Moshi = Moshi.Builder()
        .add(KotlinJsonAdapterFactory())
        .build()
    private val jsonAdapter: JsonAdapter<GetFeedResponse> =
        moshi.adapter(GetFeedResponse::class.java)

    fun testJsonStringify(): String {
        return targetContext.resources.assets.open(fileName).bufferedReader().use {
            it.readText()
        }
    }

    fun listOfJsonFeedItems(): List<FeedItem> {
        return createEntityList(jsonAdapter.fromJson(testJsonStringify())?.templatesMetadata!!).toFeedItems()
    }

    fun listOfJsonEntityItems(): List<FeedItemEntity> {
        return createEntityList(jsonAdapter.fromJson(testJsonStringify())?.templatesMetadata!!)
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

    fun List<FeedItemEntity>.toFeedItems(): List<FeedItem> {
        return map {
            FeedItem(it.id, it.thumbnailUrl, it.isPremium)
        }
    }


}