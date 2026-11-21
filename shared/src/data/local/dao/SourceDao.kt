package com.jjcr11.rss.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import com.jjcr11.rss.data.local.entity.SourceEntity

@Dao
interface SourceDao {
    @Insert
    suspend fun insert(source: SourceEntity)
}
