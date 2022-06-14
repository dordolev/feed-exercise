package com.lightricks.feedexercise.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(entities = [FeedItemEntity::class], version = 1)
abstract class FeedDatabase : RoomDatabase() {
    abstract fun feedItemDao(): FeedItemDao
}

private lateinit var INSTANCE: FeedDatabase

fun getFeedDB(context: Context): FeedDatabase {
    synchronized(FeedDatabase::class.java) {
        if (!::INSTANCE.isInitialized) {
            INSTANCE = Room.databaseBuilder(
                context,
                FeedDatabase::class.java, "FeedDatabase"
            ).build()
        }
    }
    return INSTANCE
}
