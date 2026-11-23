package com.jjcr11.rss.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.jjcr11.rss.data.local.entity.FeedEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface FeedDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(feeds: List<FeedEntity>)

    @Query("SELECT * FROM feeds ORDER BY date DESC")
    fun getAll(): Flow<List<FeedEntity>>

    @Query("UPDATE feeds SET read = 1 WHERE id = :id")
    suspend fun setAsRead(id: String): Int
}
