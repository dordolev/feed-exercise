package com.lightricks.feedexercise.database

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import com.lightricks.feedexercise.data.FeedItem
import io.reactivex.Completable
import io.reactivex.Observable
import io.reactivex.Single

@Dao
interface FeedItemDao{
    @Insert
    fun insertList(listOfFeedItems: List<FeedItem>) : Completable

    @Query("DELETE FROM FeedItems")
    fun delete() : Completable

    @Query("SELECT * FROM FeedItems")
    fun getAll(): Observable<FeedItem>

    @Query("SELECT COUNT(id) FROM FeedItems")
    fun countEntities() : Single<Int>
}
