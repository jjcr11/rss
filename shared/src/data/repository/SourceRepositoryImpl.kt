package com.jjcr11.rss.data.repository

import com.jjcr11.rss.data.local.dao.SourceDao
import com.jjcr11.rss.data.local.mapper.toDomain
import com.jjcr11.rss.data.local.mapper.toEntity
import com.jjcr11.rss.data.remote.datasource.RssRemoteDataSource
import com.jjcr11.rss.data.remote.datasource.SupabaseRemoteDataSource
import com.jjcr11.rss.data.remote.mapper.toDomain
import com.jjcr11.rss.data.remote.mapper.toDto
import com.jjcr11.rss.domain.model.Source
import com.jjcr11.rss.domain.repository.SourceRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

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
        val sources = supabaseRemoteDataSource.getSources()
        val sourcesEntity = sources.map { it.toDomain().toEntity() }
        sourceDao.insert(sourcesEntity)
    }

    override suspend fun listenToRealtimeSources() {
        supabaseRemoteDataSource.observeSources().collect { source ->
            sourceDao.insert(source.toDomain().toEntity())
        }
    }
}
