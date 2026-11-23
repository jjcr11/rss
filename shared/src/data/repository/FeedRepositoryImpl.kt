package com.jjcr11.rss.data.repository

import com.jjcr11.rss.data.local.dao.FeedDao
import com.jjcr11.rss.data.local.mapper.toDomain
import com.jjcr11.rss.data.local.mapper.toEntity
import com.jjcr11.rss.data.remote.datasource.SupabaseRemoteDataSource
import com.jjcr11.rss.data.remote.mapper.toDomain
import com.jjcr11.rss.domain.model.Feed
import com.jjcr11.rss.domain.repository.FeedRepository
import co.touchlab.kermit.Logger
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlin.coroutines.cancellation.CancellationException
import kotlin.uuid.Uuid

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

    override suspend fun setAsRead(id: Uuid) {
        val stringId = id.toString()
        val rowsAffected = feedDao.setAsRead(id = stringId)
        if (rowsAffected > 0) {
            try {
                supabaseRemoteDataSource.setFeedAsRead(id = stringId)
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                Logger.withTag(tag = TAG).w(throwable = e) {
                    "Failed to set feed as read in remote: $stringId"
                }
            }
        }
    }

    private companion object {
        const val TAG = "FeedRepositoryImpl"
    }
}
