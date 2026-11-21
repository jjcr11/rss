package com.jjcr11.rss.domain.repository

import com.jjcr11.rss.domain.model.Source
import kotlinx.coroutines.flow.Flow

interface SourceRepository {
    suspend fun insert(source: Source)

    suspend fun fetchAndSave(url: String): Source

    fun getAll(): Flow<List<Source>>
}
