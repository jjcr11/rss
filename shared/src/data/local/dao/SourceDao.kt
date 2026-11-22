package com.jjcr11.rss.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import com.jjcr11.rss.data.local.entity.FeedEntity
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
}
