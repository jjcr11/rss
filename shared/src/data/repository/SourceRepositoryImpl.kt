package com.jjcr11.rss.data.repository

import com.jjcr11.rss.data.local.dao.SourceDao
import com.jjcr11.rss.data.local.mapper.toEntity
import com.jjcr11.rss.domain.model.Source
import com.jjcr11.rss.domain.repository.SourceRepository

class SourceRepositoryImpl(
    private val sourceDao: SourceDao,
) : SourceRepository {
    override suspend fun insert(source: Source) = sourceDao.insert(source = source.toEntity())
}
