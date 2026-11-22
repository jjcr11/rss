package com.jjcr11.rss.data.repository

import com.jjcr11.rss.data.local.dao.SourceDao
import com.jjcr11.rss.data.local.entity.FeedEntity
import com.jjcr11.rss.data.local.entity.SourceEntity
import com.jjcr11.rss.data.remote.datasource.RssRemoteDataSource
import com.jjcr11.rss.data.remote.datasource.SupabaseRemoteDataSource
import com.jjcr11.rss.data.remote.dto.FeedDto
import com.jjcr11.rss.data.remote.dto.SourceDto
import com.jjcr11.rss.data.remote.parser.RssXmlParser
import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.postgrest.Postgrest
import io.ktor.client.HttpClient
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.respond
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.http.headersOf
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.time.Clock
import kotlin.uuid.Uuid

@OptIn(ExperimentalCoroutinesApi::class)
class SourceRepositoryImplTest {

    private class FakeSourceDao : SourceDao {
        val insertedSources = mutableListOf<SourceEntity>()
        val insertedFeeds = mutableListOf<FeedEntity>()
        var sourcesFlow: Flow<List<SourceEntity>> = flowOf(emptyList())

        override suspend fun insert(source: SourceEntity) {
            insertedSources.add(element = source)
        }

        override suspend fun insert(sources: List<SourceEntity>) {
            insertedSources.addAll(elements = sources)
        }

        override suspend fun insertFeeds(feeds: List<FeedEntity>) {
            insertedFeeds.addAll(elements = feeds)
        }

        override fun getAll(): Flow<List<SourceEntity>> = sourcesFlow
    }

    private class FakeSupabaseRemoteDataSource : SupabaseRemoteDataSource(
        supabaseClient = createSupabaseClient(
            supabaseUrl = "https://example.supabase.co",
            supabaseKey = "anon",
        ) {
            install(plugin = Postgrest)
        },
    ) {
        val insertedSources = mutableListOf<SourceDto>()
        val insertedFeeds = mutableListOf<FeedDto>()
        var sourcesToReturn: List<SourceDto> = emptyList()
        val sourcesFlow = MutableSharedFlow<SourceDto>(replay = 1)

        override suspend fun insertSourceWithFeeds(source: SourceDto, feeds: List<FeedDto>) {
            insertedSources.add(element = source)
            insertedFeeds.addAll(elements = feeds)
        }

        override suspend fun getSources(): List<SourceDto> = sourcesToReturn

        override fun observeSources(): Flow<SourceDto> = sourcesFlow
    }

    private fun createRepository(
        dao: FakeSourceDao,
        supabaseDataSource: FakeSupabaseRemoteDataSource = FakeSupabaseRemoteDataSource(),
        xmlResponse: String = "",
    ): SourceRepositoryImpl {
        val mockEngine = MockEngine {
            respond(
                content = xmlResponse,
                status = HttpStatusCode.OK,
                headers = headersOf(HttpHeaders.ContentType, "application/xml"),
            )
        }
        val httpClient = HttpClient(engine = mockEngine)
        val rssDataSource = RssRemoteDataSource(
            httpClient = httpClient,
            rssXmlParser = RssXmlParser(),
        )
        return SourceRepositoryImpl(
            sourceDao = dao,
            rssRemoteDataSource = rssDataSource,
            supabaseRemoteDataSource = supabaseDataSource,
        )
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

    @Test
    fun fetchAndSave_fetchesXmlSavesToDaoAndReplicatesToSupabase() = runTest {
        val dao = FakeSourceDao()
        val supabaseDataSource = FakeSupabaseRemoteDataSource()
        val repository = createRepository(
            dao = dao,
            supabaseDataSource = supabaseDataSource,
            xmlResponse = sampleRssXml,
        )

        val source = repository.fetchAndSave(url = "https://example.com/feed.xml")

        assertEquals(
            expected = "Test Feed Title",
            actual = source.name,
        )
        assertEquals(
            expected = "https://example.com/feed.xml",
            actual = source.rssUrl,
        )
        assertEquals(
            expected = "https://example.com",
            actual = source.webUrl,
        )
        assertEquals(
            expected = 1,
            actual = dao.insertedSources.size,
        )
        assertEquals(
            expected = 1,
            actual = dao.insertedFeeds.size,
        )
        assertEquals(
            expected = 1,
            actual = supabaseDataSource.insertedSources.size,
        )
        assertEquals(
            expected = 1,
            actual = supabaseDataSource.insertedFeeds.size,
        )
        assertEquals(
            expected = "Test Feed Title",
            actual = supabaseDataSource.insertedSources.first().name,
        )
        assertEquals(
            expected = "Item 1",
            actual = supabaseDataSource.insertedFeeds.first().title,
        )
    }

    @Test
    fun syncSources_fetchesFromSupabaseAndInsertsIntoDao() = runTest {
        val dao = FakeSourceDao()
        val supabaseDataSource = FakeSupabaseRemoteDataSource()
        val repository = createRepository(
            dao = dao,
            supabaseDataSource = supabaseDataSource,
        )
        val now = Clock.System.now()
        val remoteDto = SourceDto(
            id = Uuid.random().toString(),
            rssUrl = "https://example.com/feed.xml",
            webUrl = "https://example.com",
            name = "Remote Feed",
            createdAt = now.toEpochMilliseconds(),
            updatedAt = now.toEpochMilliseconds(),
        )
        supabaseDataSource.sourcesToReturn = listOf(remoteDto)

        repository.syncSources()

        assertEquals(
            expected = 1,
            actual = dao.insertedSources.size,
        )
        val entity = dao.insertedSources.first()
        assertEquals(
            expected = remoteDto.id,
            actual = entity.id,
        )
        assertEquals(
            expected = remoteDto.name,
            actual = entity.name,
        )
    }

    @Test
    fun listenToRealtimeSources_collectsFromSupabaseAndInsertsIntoDao() = runTest(context = UnconfinedTestDispatcher()) {
        val dao = FakeSourceDao()
        val supabaseDataSource = FakeSupabaseRemoteDataSource()
        val repository = createRepository(
            dao = dao,
            supabaseDataSource = supabaseDataSource,
        )
        val now = Clock.System.now()
        val remoteDto = SourceDto(
            id = Uuid.random().toString(),
            rssUrl = "https://example.com/feed.xml",
            webUrl = "https://example.com",
            name = "Realtime Feed",
            createdAt = now.toEpochMilliseconds(),
            updatedAt = now.toEpochMilliseconds(),
        )

        val job = backgroundScope.launch {
            repository.listenToRealtimeSources()
        }

        supabaseDataSource.sourcesFlow.emit(value = remoteDto)

        assertEquals(
            expected = 1,
            actual = dao.insertedSources.size,
        )
        val entity = dao.insertedSources.first()
        assertEquals(
            expected = remoteDto.id,
            actual = entity.id,
        )
        job.cancel()
    }

    @Test
    fun getAll_mapsEntitiesToDomainAndEmits() = runTest {
        val dao = FakeSourceDao()
        val repository = createRepository(dao = dao)
        val id = Uuid.random()
        val now = Clock.System.now()
        val entity = SourceEntity(
            id = id.toString(),
            rssUrl = "https://example.com/feed.xml",
            webUrl = "https://example.com",
            name = "Test Feed",
            createdAt = now.toEpochMilliseconds(),
            updatedAt = now.toEpochMilliseconds(),
        )
        dao.sourcesFlow = flowOf(value = listOf(entity))

        val sources = repository.getAll().first()

        assertEquals(
            expected = 1,
            actual = sources.size,
        )
        val domain = sources.first()
        assertEquals(
            expected = id,
            actual = domain.id,
        )
        assertEquals(
            expected = "https://example.com/feed.xml",
            actual = domain.rssUrl,
        )
        assertEquals(
            expected = "https://example.com",
            actual = domain.webUrl,
        )
        assertEquals(
            expected = "Test Feed",
            actual = domain.name,
        )
    }
}
