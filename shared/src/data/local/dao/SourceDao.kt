package com.jjcr11.rss.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.jjcr11.rss.data.local.entity.SourceEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface SourceDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(source: SourceEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(sources: List<SourceEntity>)

    @Update
    suspend fun update(source: SourceEntity)

    @Delete
    suspend fun delete(source: SourceEntity)

    @Query("DELETE FROM sources WHERE id = :id")
    suspend fun deleteById(id: String)

    @Query("SELECT * FROM sources WHERE id = :id")
    suspend fun getSourceById(id: String): SourceEntity?

    @Query("SELECT * FROM sources ORDER BY name ASC")
    fun getAllSources(): Flow<List<SourceEntity>>
}
