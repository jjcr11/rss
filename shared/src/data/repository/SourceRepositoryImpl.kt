package com.jjcr11.rss.data.repository

import co.touchlab.kermit.Logger
import com.jjcr11.rss.data.local.dao.SourceDao
import com.jjcr11.rss.data.local.entity.PendingDeletedSourceEntity
import com.jjcr11.rss.data.local.mapper.toDomain
import com.jjcr11.rss.data.local.mapper.toEntity
import com.jjcr11.rss.data.remote.datasource.RealtimeSourceEvent
import com.jjcr11.rss.data.remote.datasource.RssRemoteDataSource
import com.jjcr11.rss.data.remote.datasource.SupabaseRemoteDataSource
import com.jjcr11.rss.data.remote.mapper.toDomain
import com.jjcr11.rss.data.remote.mapper.toDto
import com.jjcr11.rss.domain.model.Source
import com.jjcr11.rss.domain.repository.SourceRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlin.coroutines.cancellation.CancellationException

class SourceRepositoryImpl(
    private val sourceDao: SourceDao,
    private val rssRemoteDataSource: RssRemoteDataSource,
    private val supabaseRemoteDataSource: SupabaseRemoteDataSource,
) : SourceRepository {
    override suspend fun fetchAndSave(url: String): Source {
        val sourceXmlDto = rssRemoteDataSource.fetchRss(url = url)
        val sourceDomain = sourceXmlDto.toDomain(rssUrl = url)
        val feedsDomain = sourceXmlDto.feeds.map { it.toDomain(sourceId = sourceDomain.id) }

        val sourceEntity = sourceDomain.toEntity()
        val feedEntities = feedsDomain.map { it.toEntity() }
        sourceDao.insertWithFeeds(source = sourceEntity, feeds = feedEntities)

        val sourceDto = sourceDomain.toDto()
        val feedsDto = feedsDomain.map { it.toDto() }
        supabaseRemoteDataSource.insertSourceWithFeeds(source = sourceDto, feeds = feedsDto)

        return sourceDomain
    }

    override fun getAll(): Flow<List<Source>> = sourceDao.getAll().map { entities ->
        entities.map { it.toDomain() }
    }

    override suspend fun syncSources() {
        val pendingIds = sourceDao.getPendingDeletedSourceIds()
        if (pendingIds.isNotEmpty()) {
            supabaseRemoteDataSource.deleteSources(ids = pendingIds)
            sourceDao.deletePendingDeletedSourcesById(ids = pendingIds)
        }

        val sources = supabaseRemoteDataSource.getSources()
        val sourcesEntity = sources.map { it.toDomain().toEntity() }
        sourceDao.insert(sources = sourcesEntity)
    }

    override suspend fun listenToRealtimeSources() {
        supabaseRemoteDataSource.observeSources().collect { event ->
            when (event) {
                is RealtimeSourceEvent.Inserted -> {
                    sourceDao.insert(source = event.source.toDomain().toEntity())
                }
                is RealtimeSourceEvent.Deleted -> {
                    sourceDao.deleteById(id = event.id)
                }
            }
        }
    }

    override suspend fun delete(source: Source): Boolean {
        val stringId = source.id.toString()
        val pendingDeletedSource = PendingDeletedSourceEntity(id = stringId)
        val rowsAffected = sourceDao.deleteLocal(
            source = source.toEntity(),
            pendingDeletedSource = pendingDeletedSource,
        )
        val isDeleted = rowsAffected > 0
        if (isDeleted) {
            try {
                supabaseRemoteDataSource.deleteSource(id = stringId)
                sourceDao.deletePendingDeletedSource(pendingDeletedSource = pendingDeletedSource)
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                Logger.withTag(tag = TAG).w(throwable = e) {
                    "Failed to delete source in remote, kept in pending: $stringId"
                }
            }
        }
        return isDeleted
    }

    private companion object {
        const val TAG = "SourceRepositoryImpl"
    }
}
