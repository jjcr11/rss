package com.jjcr11.rss.data.repository

import com.jjcr11.rss.data.local.dao.FeedDao
import com.jjcr11.rss.data.local.entity.FeedEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.time.Clock
import kotlin.uuid.Uuid

class FeedRepositoryImplTest {

    private class FakeFeedDao : FeedDao {
        var feedsFlow: Flow<List<FeedEntity>> = flowOf(emptyList())

        override fun getAll(): Flow<List<FeedEntity>> = feedsFlow
    }

    @Test
    fun getAll_mapsEntitiesToDomainAndEmits() = runTest {
        val dao = FakeFeedDao()
        val repository = FeedRepositoryImpl(feedDao = dao)
        val id = Uuid.random()
        val sourceId = Uuid.random()
        val now = Clock.System.now()
        val entity = FeedEntity(
            id = id.toString(),
            sourceId = sourceId.toString(),
            title = "Sample News",
            read = false,
            save = false,
            url = "https://example.com/news",
            date = now.toEpochMilliseconds(),
            createdAt = now.toEpochMilliseconds(),
            updatedAt = now.toEpochMilliseconds(),
        )
        dao.feedsFlow = flowOf(listOf(entity))

        val feeds = repository.getAll().first()

        assertEquals(
            expected = 1,
            actual = feeds.size,
        )
        val domain = feeds.first()
        assertEquals(
            expected = id,
            actual = domain.id,
        )
        assertEquals(
            expected = sourceId,
            actual = domain.sourceId,
        )
        assertEquals(
            expected = "Sample News",
            actual = domain.title,
        )
        assertEquals(
            expected = "https://example.com/news",
            actual = domain.url,
        )
    }
}
