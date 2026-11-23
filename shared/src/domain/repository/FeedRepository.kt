package com.jjcr11.rss.domain.repository

import com.jjcr11.rss.domain.model.Feed
import kotlinx.coroutines.flow.Flow
import kotlin.uuid.Uuid

interface FeedRepository {
    fun getAll(): Flow<List<Feed>>

    suspend fun syncFeeds()

    suspend fun setAsRead(id: Uuid)

    suspend fun fetchFeedsFromSources() {}
}
