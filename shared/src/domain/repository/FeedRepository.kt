package com.jjcr11.rss.domain.repository

import com.jjcr11.rss.domain.model.Feed
import kotlinx.coroutines.flow.Flow

interface FeedRepository {
    fun getAll(): Flow<List<Feed>>
}
