package com.jjcr11.rss.data.repository

import com.jjcr11.rss.data.local.dao.SourceDao
import com.jjcr11.rss.data.local.entity.SourceEntity
import com.jjcr11.rss.domain.model.Source
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.time.Clock
import kotlin.uuid.Uuid

class SourceRepositoryImplTest {

    private class FakeSourceDao : SourceDao {
        val insertedEntities = mutableListOf<SourceEntity>()

        override suspend fun insert(source: SourceEntity) {
            insertedEntities.add(element = source)
        }
    }

    @Test
    fun insert_mapsToEntityAndCallsDao() = runTest {
        val dao = FakeSourceDao()
        val repository = SourceRepositoryImpl(sourceDao = dao)
        val id = Uuid.random()
        val now = Clock.System.now()
        val source = Source(
            id = id,
            rssUrl = "https://example.com/feed.xml",
            webUrl = "https://example.com",
            name = "Test Feed",
            createdAt = now,
            updatedAt = now,
        )

        repository.insert(source = source)

        assertEquals(
            expected = 1,
            actual = dao.insertedEntities.size,
        )
        val entity = dao.insertedEntities.first()
        assertEquals(
            expected = id.toString(),
            actual = entity.id,
        )
        assertEquals(
            expected = "https://example.com/feed.xml",
            actual = entity.rssUrl,
        )
        assertEquals(
            expected = "https://example.com",
            actual = entity.webUrl,
        )
        assertEquals(
            expected = "Test Feed",
            actual = entity.name,
        )
        assertEquals(
            expected = now.toEpochMilliseconds(),
            actual = entity.createdAt,
        )
        assertEquals(
            expected = now.toEpochMilliseconds(),
            actual = entity.updatedAt,
        )
    }
}
