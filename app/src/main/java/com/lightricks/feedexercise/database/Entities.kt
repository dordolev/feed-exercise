package com.lightricks.feedexercise.database

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class FeedItems(
    @PrimaryKey val id: Int,
    val thumbnailUrl: String,
    val isPremium: Boolean
)
