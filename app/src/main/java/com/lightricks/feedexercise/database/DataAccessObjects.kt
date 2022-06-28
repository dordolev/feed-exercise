package com.lightricks.feedexercise.database

import androidx.lifecycle.LiveData
import androidx.room.*
import com.lightricks.feedexercise.data.FeedItem
import io.reactivex.Completable
import io.reactivex.Observable
import io.reactivex.Single

@Dao
interface FeedItemDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertList(listOfFeedItems: List<FeedItemEntity>): Completable

    @Query("DELETE FROM feed_items_table")
    fun delete(): Completable

    @Query("SELECT * FROM feed_items_table")
    fun getAll(): LiveData<List<FeedItemEntity>>

    @Query("SELECT COUNT(*) FROM feed_items_table")
    fun countEntities(): Int
}
