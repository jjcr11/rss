package com.jjcr11.rss.data.repository

import com.jjcr11.rss.data.local.dao.FeedDao
import com.jjcr11.rss.data.local.dao.SourceDao
import com.jjcr11.rss.data.local.mapper.toDomain
import com.jjcr11.rss.data.local.mapper.toEntity
import com.jjcr11.rss.data.remote.datasource.RssRemoteDataSource
import com.jjcr11.rss.data.remote.datasource.SupabaseRemoteDataSource
import com.jjcr11.rss.data.remote.mapper.toDomain
import com.jjcr11.rss.data.remote.mapper.toDto
import com.jjcr11.rss.domain.model.Feed
import com.jjcr11.rss.domain.repository.FeedRepository
import co.touchlab.kermit.Logger
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlin.coroutines.cancellation.CancellationException
import kotlin.uuid.Uuid

class FeedRepositoryImpl(
    private val feedDao: FeedDao,
    private val supabaseRemoteDataSource: SupabaseRemoteDataSource,
    private val sourceDao: SourceDao? = null,
    private val rssRemoteDataSource: RssRemoteDataSource? = null,
) : FeedRepository {
    override fun getAll(): Flow<List<Feed>> = feedDao.getAll().map { entities ->
        entities.map { it.toDomain() }
    }

    override suspend fun syncFeeds() {
        val ids = feedDao.getPendingSyncIds()
        if (ids.isNotEmpty()) {
            supabaseRemoteDataSource.setFeedsAsRead(ids = ids)
            feedDao.setAsSynced(ids = ids)
        }


        val feeds = supabaseRemoteDataSource.getUnreadFeeds()
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
                feedDao.setAsSynced(id = stringId)
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                Logger.withTag(tag = TAG).w(throwable = e) {
                    "Failed to set feed as read in remote: $stringId"
                }
            }
        }
    }

    override suspend fun fetchFeedsFromSources() {
        val sDao = sourceDao ?: return
        val rssDataSource = rssRemoteDataSource ?: return

        val sources = sDao.getAll().first()
        for (source in sources) {
            try {
                val sourceXmlDto = rssDataSource.fetchRss(url = source.rssUrl)
                val sourceId = Uuid.parse(uuidString = source.id)
                val feedsDomain = sourceXmlDto.feeds.map { it.toDomain(sourceId = sourceId) }
                val existingUrls = feedDao.getUrlsBySourceId(sourceId = source.id).toSet()
                val newFeeds = feedsDomain.filterNot { feed ->
                    feed.url in existingUrls
                }

                if (newFeeds.isNotEmpty()) {
                    val newEntities = newFeeds.map { it.toEntity() }
                    feedDao.insert(feeds = newEntities)

                    val newDtos = newFeeds.map { it.toDto() }
                    try {
                        supabaseRemoteDataSource.insertFeeds(feeds = newDtos)
                    } catch (e: CancellationException) {
                        throw e
                    } catch (e: Exception) {
                        Logger.withTag(tag = TAG).w(throwable = e) {
                            "Failed to replicate new feeds to Supabase for source ${source.id}"
                        }
                    }
                }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                Logger.withTag(tag = TAG).w(throwable = e) {
                    "Failed to fetch RSS for source: ${source.rssUrl}"
                }
            }
        }
    }

    private companion object {
        const val TAG = "FeedRepositoryImpl"
    }
}
