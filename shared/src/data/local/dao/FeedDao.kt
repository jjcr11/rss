package com.jjcr11.rss.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.jjcr11.rss.data.local.entity.FeedEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface FeedDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(feed: FeedEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(feeds: List<FeedEntity>)

    @Update
    suspend fun update(feed: FeedEntity)

    @Delete
    suspend fun delete(feed: FeedEntity)

    @Query("DELETE FROM feeds WHERE id = :id")
    suspend fun deleteById(id: String)

    @Query("SELECT * FROM feeds WHERE id = :id")
    suspend fun getFeedById(id: String): FeedEntity?

    @Query("SELECT * FROM feeds ORDER BY date DESC")
    fun getAllFeeds(): Flow<List<FeedEntity>>

    @Query("SELECT * FROM feeds WHERE source_id = :sourceId ORDER BY date DESC")
    fun getFeedsBySourceId(sourceId: String): Flow<List<FeedEntity>>

    @Query("UPDATE feeds SET read = :read WHERE id = :id")
    suspend fun markAsRead(id: String, read: Boolean)

    @Query("UPDATE feeds SET save = :save WHERE id = :id")
    suspend fun toggleSave(id: String, save: Boolean)
}
