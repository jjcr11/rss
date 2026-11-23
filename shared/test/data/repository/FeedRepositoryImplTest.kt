package com.jjcr11.rss.data.repository

import com.jjcr11.rss.data.local.dao.FeedDao
import com.jjcr11.rss.data.local.entity.FeedEntity
import com.jjcr11.rss.data.remote.datasource.SupabaseRemoteDataSource
import com.jjcr11.rss.data.remote.dto.FeedDto
import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.postgrest.Postgrest
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
        var feedsFlow: Flow<List<FeedEntity>> = flowOf(value = emptyList())
        val insertedFeeds = mutableListOf<FeedEntity>()

        override suspend fun insert(feeds: List<FeedEntity>) {
            insertedFeeds.addAll(elements = feeds)
        }

        override fun getAll(): Flow<List<FeedEntity>> = feedsFlow

        var setAsReadId: String? = null
        var setAsReadResult: Int = 1

        override suspend fun setAsRead(id: String): Int {
            setAsReadId = id
            return setAsReadResult
        }
    }

    private class FakeSupabaseRemoteDataSource : SupabaseRemoteDataSource(
        supabaseClient = createSupabaseClient(
            supabaseUrl = "https://example.supabase.co",
            supabaseKey = "anon",
        ) {
            install(plugin = Postgrest)
        },
    ) {
        var feedsToReturn: List<FeedDto> = emptyList()
        var setFeedAsReadId: String? = null
        var setFeedAsReadShouldThrow: Exception? = null

        override suspend fun getFeeds(): List<FeedDto> = feedsToReturn

        override suspend fun setFeedAsRead(id: String) {
            setFeedAsReadId = id
            setFeedAsReadShouldThrow?.let { throw it }
        }
    }

    @Test
    fun getAll_mapsEntitiesToDomainAndEmits() = runTest {
        val dao = FakeFeedDao()
        val supabase = FakeSupabaseRemoteDataSource()
        val repository = FeedRepositoryImpl(
            feedDao = dao,
            supabaseRemoteDataSource = supabase,
        )
        val id = Uuid.random()
        val sourceId = Uuid.random()
        val now = Clock.System.now()
        val entity = FeedEntity(
            id = id.toString(),
            sourceId = sourceId.toString(),
            title = "Sample News",
            content = "Sample content",
            read = false,
            save = false,
            url = "https://example.com/news",
            date = now.toEpochMilliseconds(),
            createdAt = now.toEpochMilliseconds(),
            updatedAt = now.toEpochMilliseconds(),
        )
        dao.feedsFlow = flowOf(value = listOf(entity))

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
            expected = "Sample content",
            actual = domain.content,
        )
        assertEquals(
            expected = "https://example.com/news",
            actual = domain.url,
        )
    }

    @Test
    fun syncFeeds_fetchesFromSupabaseAndInsertsIntoFeedDao() = runTest {
        val dao = FakeFeedDao()
        val supabase = FakeSupabaseRemoteDataSource()
        val repository = FeedRepositoryImpl(
            feedDao = dao,
            supabaseRemoteDataSource = supabase,
        )

        val id = Uuid.random()
        val sourceId = Uuid.random()
        val now = Clock.System.now()
        val dto = FeedDto(
            id = id.toString(),
            sourceId = sourceId.toString(),
            title = "Supabase Feed",
            content = "Supabase Content",
            read = false,
            save = false,
            url = "https://example.com/supabase-feed",
            date = now.toEpochMilliseconds(),
            createdAt = now.toEpochMilliseconds(),
            updatedAt = now.toEpochMilliseconds(),
        )
        supabase.feedsToReturn = listOf(dto)

        repository.syncFeeds()

        assertEquals(
            expected = 1,
            actual = dao.insertedFeeds.size,
        )
        val inserted = dao.insertedFeeds.first()
        assertEquals(
            expected = id.toString(),
            actual = inserted.id,
        )
        assertEquals(
            expected = sourceId.toString(),
            actual = inserted.sourceId,
        )
        assertEquals(
            expected = "Supabase Feed",
            actual = inserted.title,
        )
        assertEquals(
            expected = "Supabase Content",
            actual = inserted.content,
        )
    }

    @Test
    fun setAsRead_whenRowsAffectedGreaterThanZero_updatesDaoAndSupabase() = runTest {
        val dao = FakeFeedDao()
        val supabase = FakeSupabaseRemoteDataSource()
        val repository = FeedRepositoryImpl(
            feedDao = dao,
            supabaseRemoteDataSource = supabase,
        )
        val feedId = Uuid.random()

        repository.setAsRead(id = feedId)

        assertEquals(
            expected = feedId.toString(),
            actual = dao.setAsReadId,
        )
        assertEquals(
            expected = feedId.toString(),
            actual = supabase.setFeedAsReadId,
        )
    }

    @Test
    fun setAsRead_whenZeroRowsAffected_doesNotCallSupabase() = runTest {
        val dao = FakeFeedDao()
        dao.setAsReadResult = 0
        val supabase = FakeSupabaseRemoteDataSource()
        val repository = FeedRepositoryImpl(
            feedDao = dao,
            supabaseRemoteDataSource = supabase,
        )
        val feedId = Uuid.random()

        repository.setAsRead(id = feedId)

        assertEquals(
            expected = feedId.toString(),
            actual = dao.setAsReadId,
        )
        assertEquals(
            expected = null,
            actual = supabase.setFeedAsReadId,
        )
    }
}
