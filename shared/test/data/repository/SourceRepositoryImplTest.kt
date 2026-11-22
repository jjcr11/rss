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
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.time.Clock
import kotlin.uuid.Uuid

class SourceRepositoryImplTest {

    private class FakeSourceDao : SourceDao {
        val insertedSources = mutableListOf<SourceEntity>()
        val insertedFeeds = mutableListOf<FeedEntity>()
        var sourcesFlow: Flow<List<SourceEntity>> = flowOf(emptyList())

        override suspend fun insert(source: SourceEntity) {
            insertedSources.add(element = source)
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

        override suspend fun insertSourceWithFeeds(source: SourceDto, feeds: List<FeedDto>) {
            insertedSources.add(element = source)
            insertedFeeds.addAll(elements = feeds)
        }
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
                headers = headersOf(
                    name = HttpHeaders.ContentType,
                    value = "application/rss+xml; charset=utf-8",
                ),
            )
        }
        val remoteDataSource = RssRemoteDataSource(
            httpClient = HttpClient(engine = mockEngine),
            rssXmlParser = RssXmlParser(),
        )
        return SourceRepositoryImpl(
            sourceDao = dao,
            rssRemoteDataSource = remoteDataSource,
            supabaseRemoteDataSource = supabaseDataSource,
        )
    }

    @Test
    fun fetchAndSave_fetchesRssAndInsertsSourceWithFeedsAndSavesToSupabase() = runTest {
        val xml = """
            <?xml version="1.0" encoding="UTF-8"?>
            <rss version="2.0">
                <channel>
                    <title>Kotaku</title>
                    <link>https://example.com</link>
                    <item>
                        <title>Sample Article</title>
                        <link>https://example.com/sample</link>
                    </item>
                </channel>
            </rss>
        """.trimIndent()

        val dao = FakeSourceDao()
        val supabaseDataSource = FakeSupabaseRemoteDataSource()
        val repository = createRepository(
            dao = dao,
            supabaseDataSource = supabaseDataSource,
            xmlResponse = xml,
        )

        val source = repository.fetchAndSave(url = "https://example.com/feed.xml")

        assertEquals(
            expected = "Kotaku",
            actual = source.name,
        )
        assertEquals(
            expected = "https://example.com/feed.xml",
            actual = source.rssUrl,
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
            expected = "Sample Article",
            actual = dao.insertedFeeds.first().title,
        )
        assertEquals(
            expected = source.id.toString(),
            actual = dao.insertedFeeds.first().sourceId,
        )
        assertEquals(
            expected = 1,
            actual = supabaseDataSource.insertedSources.size,
        )
        assertEquals(
            expected = source.id.toString(),
            actual = supabaseDataSource.insertedSources.first().id,
        )
        assertEquals(
            expected = 1,
            actual = supabaseDataSource.insertedFeeds.size,
        )
        assertEquals(
            expected = "Sample Article",
            actual = supabaseDataSource.insertedFeeds.first().title,
        )
        assertEquals(
            expected = source.id.toString(),
            actual = supabaseDataSource.insertedFeeds.first().sourceId,
        )
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
