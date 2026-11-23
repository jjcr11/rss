package com.jjcr11.rss.data.repository

import com.jjcr11.rss.data.local.dao.FeedDao
import com.jjcr11.rss.data.local.dao.SourceDao
import com.jjcr11.rss.data.local.entity.FeedEntity
import com.jjcr11.rss.data.local.entity.PendingDeletedSourceEntity
import com.jjcr11.rss.data.local.entity.SourceEntity
import com.jjcr11.rss.data.remote.datasource.RssRemoteDataSource
import com.jjcr11.rss.data.remote.datasource.SupabaseRemoteDataSource
import com.jjcr11.rss.data.remote.dto.FeedDto
import com.jjcr11.rss.data.remote.parser.RssXmlParser
import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.postgrest.Postgrest
import io.ktor.client.HttpClient
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.respond
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.http.headersOf
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import kotlin.coroutines.cancellation.CancellationException
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
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

        var setAsSyncedId: String? = null
        var setAsSyncedIds: List<String>? = null
        var pendingSyncFeedIds: List<String> = emptyList()

        override suspend fun setAsSynced(id: String): Int {
            setAsSyncedId = id
            return 1
        }

        override suspend fun setAsSynced(ids: List<String>): Int {
            setAsSyncedIds = ids
            return ids.size
        }

        override suspend fun getPendingSyncIds(): List<String> = pendingSyncFeedIds

        var urlsBySourceId: Map<String, List<String>> = emptyMap()

        override suspend fun getUrlsBySourceId(sourceId: String): List<String> =
            urlsBySourceId[sourceId] ?: emptyList()
    }

    private class FakeSourceDao : SourceDao {
        var sourcesFlow: Flow<List<SourceEntity>> = flowOf(value = emptyList())

        override suspend fun insert(source: SourceEntity) = Unit
        override suspend fun insert(sources: List<SourceEntity>) = Unit
        override suspend fun insertFeeds(feeds: List<FeedEntity>) = Unit
        override fun getAll(): Flow<List<SourceEntity>> = sourcesFlow
        override suspend fun delete(source: SourceEntity): Int = 1
        override suspend fun deleteFeedsBySourceId(sourceId: String): Int = 0
        override suspend fun deleteSourceById(id: String): Int = 1
        override suspend fun deleteById(id: String): Int = 1
        override suspend fun insertPendingDeletedSource(pendingDeletedSource: PendingDeletedSourceEntity) = Unit
        override suspend fun getPendingDeletedSourceIds(): List<String> = emptyList()
        override suspend fun deletePendingDeletedSource(pendingDeletedSource: PendingDeletedSourceEntity): Int = 1
        override suspend fun deletePendingDeletedSourcesById(ids: List<String>): Int = ids.size
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
        var setFeedsAsReadIds: List<String>? = null
        var setFeedsAsReadShouldThrow: Exception? = null
        val insertedFeeds = mutableListOf<FeedDto>()
        var insertFeedsShouldThrow: Exception? = null

        override suspend fun getUnreadFeeds(): List<FeedDto> = feedsToReturn

        override suspend fun setFeedAsRead(id: String) {
            setFeedAsReadId = id
            setFeedAsReadShouldThrow?.let { throw it }
        }

        override suspend fun setFeedsAsRead(ids: List<String>) {
            setFeedsAsReadIds = ids
            setFeedsAsReadShouldThrow?.let { throw it }
        }

        override suspend fun insertFeeds(feeds: List<FeedDto>) {
            insertFeedsShouldThrow?.let { throw it }
            insertedFeeds.addAll(elements = feeds)
        }
    }

    private val sampleRssXml = """
        <?xml version="1.0" encoding="UTF-8"?>
        <rss version="2.0">
            <channel>
                <title>Test Feed Title</title>
                <link>https://example.com</link>
                <description>Test Description</description>
                <item>
                    <title>Item 1</title>
                    <link>https://example.com/item1</link>
                    <description>Item 1 Description</description>
                    <pubDate>Mon, 01 Jan 2024 12:00:00 GMT</pubDate>
                </item>
            </channel>
        </rss>
    """.trimIndent()

    private fun createRssRemoteDataSource(
        mockEngine: MockEngine,
    ): RssRemoteDataSource = RssRemoteDataSource(
        httpClient = HttpClient(engine = mockEngine),
        rssXmlParser = RssXmlParser(),
    )

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
            isRead = false,
            isSaved = false,
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
            isRead = false,
            isSaved = false,
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
    fun syncFeeds_whenPendingSyncExists_syncsPendingFeedsToSupabaseAndMarksAsSynced() = runTest {
        val dao = FakeFeedDao()
        dao.pendingSyncFeedIds = listOf("feed-1", "feed-2")
        val supabase = FakeSupabaseRemoteDataSource()
        val repository = FeedRepositoryImpl(
            feedDao = dao,
            supabaseRemoteDataSource = supabase,
        )

        repository.syncFeeds()

        assertEquals(
            expected = listOf("feed-1", "feed-2"),
            actual = supabase.setFeedsAsReadIds,
        )
        assertEquals(
            expected = listOf("feed-1", "feed-2"),
            actual = dao.setAsSyncedIds,
        )
    }

    @Test
    fun syncFeeds_whenPendingSyncThrows_doesNotFetchOrInsertRemoteFeeds() = runTest {
        val dao = FakeFeedDao()
        dao.pendingSyncFeedIds = listOf("feed-1")
        val supabase = FakeSupabaseRemoteDataSource()
        supabase.setFeedsAsReadShouldThrow = RuntimeException("Network error")
        val repository = FeedRepositoryImpl(
            feedDao = dao,
            supabaseRemoteDataSource = supabase,
        )

        try {
            repository.syncFeeds()
        } catch (_: Exception) {
        }

        assertEquals(
            expected = null,
            actual = dao.setAsSyncedIds,
        )
        assertEquals(
            expected = 0,
            actual = dao.insertedFeeds.size,
        )
    }

    @Test
    fun setAsRead_whenRowsAffectedGreaterThanZero_updatesDaoAndSupabaseAndMarksSynced() = runTest {
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
        assertEquals(
            expected = feedId.toString(),
            actual = dao.setAsSyncedId,
        )
    }

    @Test
    fun setAsRead_whenSupabaseThrows_catchesExceptionAndDoesNotMarkSynced() = runTest {
        val dao = FakeFeedDao()
        val supabase = FakeSupabaseRemoteDataSource()
        supabase.setFeedAsReadShouldThrow = RuntimeException("Network error")
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
            actual = dao.setAsSyncedId,
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

    @Test
    fun fetchFeedsFromSources_whenDependenciesNull_doesNothing() = runTest {
        val feedDao = FakeFeedDao()
        val supabase = FakeSupabaseRemoteDataSource()
        val repository = FeedRepositoryImpl(
            feedDao = feedDao,
            supabaseRemoteDataSource = supabase,
        )

        repository.fetchFeedsFromSources()

        assertEquals(
            expected = 0,
            actual = feedDao.insertedFeeds.size,
        )
    }

    @Test
    fun fetchFeedsFromSources_whenSourcesEmpty_doesNothing() = runTest {
        val feedDao = FakeFeedDao()
        val sourceDao = FakeSourceDao()
        val supabase = FakeSupabaseRemoteDataSource()
        val mockEngine = MockEngine {
            respond(
                content = sampleRssXml,
                status = HttpStatusCode.OK,
                headers = headersOf(HttpHeaders.ContentType, "application/xml"),
            )
        }
        val repository = FeedRepositoryImpl(
            feedDao = feedDao,
            supabaseRemoteDataSource = supabase,
            sourceDao = sourceDao,
            rssRemoteDataSource = createRssRemoteDataSource(mockEngine = mockEngine),
        )

        repository.fetchFeedsFromSources()

        assertEquals(
            expected = 0,
            actual = feedDao.insertedFeeds.size,
        )
    }

    @Test
    fun fetchFeedsFromSources_whenNewFeedsAvailable_insertsIntoDaoAndSupabase() = runTest {
        val feedDao = FakeFeedDao()
        val sourceDao = FakeSourceDao()
        val supabase = FakeSupabaseRemoteDataSource()
        val sourceId = Uuid.random().toString()
        val source = SourceEntity(
            id = sourceId,
            rssUrl = "https://example.com/rss",
            webUrl = "https://example.com",
            name = "Test Source",
            createdAt = 1000L,
            updatedAt = 1000L,
        )
        sourceDao.sourcesFlow = flowOf(value = listOf(source))

        val mockEngine = MockEngine {
            respond(
                content = sampleRssXml,
                status = HttpStatusCode.OK,
                headers = headersOf(HttpHeaders.ContentType, "application/xml"),
            )
        }
        val repository = FeedRepositoryImpl(
            feedDao = feedDao,
            supabaseRemoteDataSource = supabase,
            sourceDao = sourceDao,
            rssRemoteDataSource = createRssRemoteDataSource(mockEngine = mockEngine),
        )

        repository.fetchFeedsFromSources()

        assertEquals(
            expected = 1,
            actual = feedDao.insertedFeeds.size,
        )
        assertEquals(
            expected = "https://example.com/item1",
            actual = feedDao.insertedFeeds.first().url,
        )
        assertEquals(
            expected = 1,
            actual = supabase.insertedFeeds.size,
        )
        assertEquals(
            expected = "https://example.com/item1",
            actual = supabase.insertedFeeds.first().url,
        )
    }

    @Test
    fun fetchFeedsFromSources_whenAllFeedsAlreadyExist_doesNotInsertIntoDaoOrSupabase() = runTest {
        val feedDao = FakeFeedDao()
        val sourceDao = FakeSourceDao()
        val supabase = FakeSupabaseRemoteDataSource()
        val sourceId = Uuid.random().toString()
        val source = SourceEntity(
            id = sourceId,
            rssUrl = "https://example.com/rss",
            webUrl = "https://example.com",
            name = "Test Source",
            createdAt = 1000L,
            updatedAt = 1000L,
        )
        sourceDao.sourcesFlow = flowOf(value = listOf(source))
        feedDao.urlsBySourceId = mapOf(sourceId to listOf("https://example.com/item1"))

        val mockEngine = MockEngine {
            respond(
                content = sampleRssXml,
                status = HttpStatusCode.OK,
                headers = headersOf(HttpHeaders.ContentType, "application/xml"),
            )
        }
        val repository = FeedRepositoryImpl(
            feedDao = feedDao,
            supabaseRemoteDataSource = supabase,
            sourceDao = sourceDao,
            rssRemoteDataSource = createRssRemoteDataSource(mockEngine = mockEngine),
        )

        repository.fetchFeedsFromSources()

        assertEquals(
            expected = 0,
            actual = feedDao.insertedFeeds.size,
        )
        assertEquals(
            expected = 0,
            actual = supabase.insertedFeeds.size,
        )
    }

    @Test
    fun fetchFeedsFromSources_whenRssFetchFails_catchesExceptionAndContinuesWithNextSource() = runTest {
        val feedDao = FakeFeedDao()
        val sourceDao = FakeSourceDao()
        val supabase = FakeSupabaseRemoteDataSource()
        val sourceId1 = Uuid.random().toString()
        val source1 = SourceEntity(
            id = sourceId1,
            rssUrl = "https://failing.com/rss",
            webUrl = "https://failing.com",
            name = "Failing Source",
            createdAt = 1000L,
            updatedAt = 1000L,
        )
        val sourceId2 = Uuid.random().toString()
        val source2 = SourceEntity(
            id = sourceId2,
            rssUrl = "https://success.com/rss",
            webUrl = "https://success.com",
            name = "Success Source",
            createdAt = 1000L,
            updatedAt = 1000L,
        )
        sourceDao.sourcesFlow = flowOf(value = listOf(source1, source2))

        val mockEngine = MockEngine { request ->
            if (request.url.toString() == "https://failing.com/rss") {
                throw RuntimeException("Network down")
            }
            respond(
                content = sampleRssXml,
                status = HttpStatusCode.OK,
                headers = headersOf(HttpHeaders.ContentType, "application/xml"),
            )
        }
        val repository = FeedRepositoryImpl(
            feedDao = feedDao,
            supabaseRemoteDataSource = supabase,
            sourceDao = sourceDao,
            rssRemoteDataSource = createRssRemoteDataSource(mockEngine = mockEngine),
        )

        repository.fetchFeedsFromSources()

        assertEquals(
            expected = 1,
            actual = feedDao.insertedFeeds.size,
        )
        assertEquals(
            expected = "https://example.com/item1",
            actual = feedDao.insertedFeeds.first().url,
        )
    }

    @Test
    fun fetchFeedsFromSources_whenSupabaseInsertFails_catchesExceptionAndDoesNotThrow() = runTest {
        val feedDao = FakeFeedDao()
        val sourceDao = FakeSourceDao()
        val supabase = FakeSupabaseRemoteDataSource().apply {
            insertFeedsShouldThrow = RuntimeException("Supabase offline")
        }
        val sourceId = Uuid.random().toString()
        val source = SourceEntity(
            id = sourceId,
            rssUrl = "https://example.com/rss",
            webUrl = "https://example.com",
            name = "Test Source",
            createdAt = 1000L,
            updatedAt = 1000L,
        )
        sourceDao.sourcesFlow = flowOf(value = listOf(source))

        val mockEngine = MockEngine {
            respond(
                content = sampleRssXml,
                status = HttpStatusCode.OK,
                headers = headersOf(HttpHeaders.ContentType, "application/xml"),
            )
        }
        val repository = FeedRepositoryImpl(
            feedDao = feedDao,
            supabaseRemoteDataSource = supabase,
            sourceDao = sourceDao,
            rssRemoteDataSource = createRssRemoteDataSource(mockEngine = mockEngine),
        )

        repository.fetchFeedsFromSources()

        assertEquals(
            expected = 1,
            actual = feedDao.insertedFeeds.size,
        )
    }

    @Test
    fun fetchFeedsFromSources_whenCancellationExceptionThrown_rethrowsCleanly() = runTest {
        val feedDao = FakeFeedDao()
        val sourceDao = FakeSourceDao()
        val supabase = FakeSupabaseRemoteDataSource()
        val sourceId = Uuid.random().toString()
        val source = SourceEntity(
            id = sourceId,
            rssUrl = "https://example.com/rss",
            webUrl = "https://example.com",
            name = "Test Source",
            createdAt = 1000L,
            updatedAt = 1000L,
        )
        sourceDao.sourcesFlow = flowOf(value = listOf(source))

        val mockEngine = MockEngine {
            throw CancellationException("Cancelled")
        }
        val repository = FeedRepositoryImpl(
            feedDao = feedDao,
            supabaseRemoteDataSource = supabase,
            sourceDao = sourceDao,
            rssRemoteDataSource = createRssRemoteDataSource(mockEngine = mockEngine),
        )

        assertFailsWith<CancellationException> {
            repository.fetchFeedsFromSources()
        }
    }
}
