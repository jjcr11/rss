package com.jjcr11.rss.data.repository

import com.jjcr11.rss.data.local.dao.SourceDao
import com.jjcr11.rss.data.local.mapper.toDomain
import com.jjcr11.rss.data.local.mapper.toEntity
import com.jjcr11.rss.data.remote.datasource.RssRemoteDataSource
import com.jjcr11.rss.data.remote.mapper.toDomain
import com.jjcr11.rss.domain.model.Source
import com.jjcr11.rss.domain.repository.SourceRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class SourceRepositoryImpl(
    private val sourceDao: SourceDao,
    private val rssRemoteDataSource: RssRemoteDataSource,
) : SourceRepository {
    override suspend fun insert(source: Source) = sourceDao.insert(source = source.toEntity())

    override suspend fun fetchAndSave(url: String): Source {
        val sourceXmlDto = rssRemoteDataSource.fetchRss(url = url)
        val sourceDomain = sourceXmlDto.toDomain(rssUrl = url)
        val sourceEntity = sourceDomain.toEntity()
        val feedsDomain = sourceXmlDto.feeds.map { it.toDomain(sourceId = sourceDomain.id) }
        val feedEntities = feedsDomain.map { it.toEntity() }
        sourceDao.insertWithFeeds(source = sourceEntity, feeds = feedEntities)
        return sourceDomain
    }

    override fun getAll(): Flow<List<Source>> = sourceDao.getAll().map { entities ->
        entities.map { it.toDomain() }
    }
}
