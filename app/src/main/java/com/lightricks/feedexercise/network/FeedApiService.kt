package com.lightricks.feedexercise.network

import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import io.reactivex.Single
import retrofit2.Retrofit
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.http.GET

/**
 * todo: add the FeedApiService interface and the Retrofit and Moshi code here
 */

interface FeedApiService {
    @GET("Android/demo/feed.json")
    fun getResponse(): Single<GetFeedResponse>
}

object FeedApiServiceImpl {
        private val moshi: Moshi =
            Moshi.Builder()
                .add(KotlinJsonAdapterFactory())
                .build()

        private val retrofit: Retrofit =
            Retrofit.Builder()
                .baseUrl("https://assets.swishvideoapp.com")
                .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                .addConverterFactory(MoshiConverterFactory.create(moshi))
                .build()

        val service: FeedApiService = retrofit.create(FeedApiService::class.java)
    }