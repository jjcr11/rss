package com.jjcr11.rss.data.local.dao

import androidx.room.Dao
import androidx.room.Query
import com.jjcr11.rss.data.local.entity.FeedEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface FeedDao {
    @Query("SELECT * FROM feeds ORDER BY date DESC")
    fun getAll(): Flow<List<FeedEntity>>
}
