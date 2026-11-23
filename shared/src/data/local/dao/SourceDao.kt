package com.jjcr11.rss.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import com.jjcr11.rss.data.local.entity.FeedEntity
import com.jjcr11.rss.data.local.entity.PendingDeletedSourceEntity
import com.jjcr11.rss.data.local.entity.SourceEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface SourceDao {
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(source: SourceEntity)

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(sources: List<SourceEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFeeds(feeds: List<FeedEntity>)

    @Transaction
    suspend fun insertWithFeeds(source: SourceEntity, feeds: List<FeedEntity>) {
        insert(source = source)
        insertFeeds(feeds = feeds)
    }

    @Query("SELECT * FROM sources ORDER BY created_at DESC")
    fun getAll(): Flow<List<SourceEntity>>

    @Delete
    suspend fun delete(source: SourceEntity): Int

    @Insert
    suspend fun insertPendingDeletedSource(pendingDeletedSource: PendingDeletedSourceEntity)

    @Transaction
    suspend fun deleteLocal(
        source: SourceEntity,
        pendingDeletedSource: PendingDeletedSourceEntity,
    ): Int {
        insertPendingDeletedSource(pendingDeletedSource)
        return delete(source)
    }

    @Query("SELECT id FROM pending_deleted_sources")
    suspend fun getPendingDeletedSourceIds(): List<String>

    @Delete
    suspend fun deletePendingDeletedSource(pendingDeletedSource: PendingDeletedSourceEntity): Int

    @Query("DELETE FROM pending_deleted_sources WHERE id in (:ids)")
    suspend fun deletePendingDeletedSourcesById(ids: List<String>): Int
}
