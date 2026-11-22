package com.jjcr11.rss.data.repository

import com.jjcr11.rss.data.local.dao.FeedDao
import com.jjcr11.rss.data.local.mapper.toDomain
import com.jjcr11.rss.data.local.mapper.toEntity
import com.jjcr11.rss.data.remote.datasource.SupabaseRemoteDataSource
import com.jjcr11.rss.data.remote.mapper.toDomain
import com.jjcr11.rss.domain.model.Feed
import com.jjcr11.rss.domain.repository.FeedRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class FeedRepositoryImpl(
    private val feedDao: FeedDao,
    private val supabaseRemoteDataSource: SupabaseRemoteDataSource,
) : FeedRepository {
    override fun getAll(): Flow<List<Feed>> = feedDao.getAll().map { entities ->
        entities.map { it.toDomain() }
    }

    override suspend fun syncFeeds() {
        val feeds = supabaseRemoteDataSource.getFeeds()
        val feedsEntity = feeds.map { it.toDomain().toEntity() }
        if (feedsEntity.isNotEmpty()) {
            feedDao.insert(feeds = feedsEntity)
        }
    }
}
