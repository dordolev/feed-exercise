package com.lightricks.feedexercise.network

import androidx.test.platform.app.InstrumentationRegistry
import com.lightricks.feedexercise.data.*

import com.squareup.moshi.JsonAdapter
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import io.reactivex.Single

class FeedApiServiceMock(jsonTestItems: JsonTestItems) : FeedApiService {
    private val moshi: Moshi = Moshi.Builder()
        .add(KotlinJsonAdapterFactory())
        .build()
    private val jsonAdapter: JsonAdapter<GetFeedResponse> =
        moshi.adapter(GetFeedResponse::class.java)
    private val jsonTestItems: JsonTestItems = jsonTestItems

    override fun getFeed(): Single<GetFeedResponse> {
        return Single.just(jsonAdapter.fromJson(jsonTestItems.testJsonStringify()))
    }
}