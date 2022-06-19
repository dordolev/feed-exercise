package com.lightricks.feedexercise.database

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "feed_items_table")
data class FeedItemEntity(
    @PrimaryKey val id: String,
    val thumbnailUrl: String,
    val isPremium: Boolean
)
