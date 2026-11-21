package com.jjcr11.rss.data.repository

import com.jjcr11.rss.data.local.dao.SourceDao
import com.jjcr11.rss.data.local.entity.FeedEntity
import com.jjcr11.rss.data.local.entity.SourceEntity
import com.jjcr11.rss.data.remote.datasource.RssRemoteDataSource
import com.jjcr11.rss.data.remote.parser.RssXmlParser
import com.jjcr11.rss.domain.model.Source
import io.ktor.client.HttpClient
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.respond
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.http.headersOf
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.time.Clock
import kotlin.uuid.Uuid

class SourceRepositoryImplTest {

    private class FakeSourceDao : SourceDao {
        val insertedSources = mutableListOf<SourceEntity>()
        val insertedFeeds = mutableListOf<FeedEntity>()

        override suspend fun insert(source: SourceEntity) {
            insertedSources.add(element = source)
        }

        override suspend fun insertFeeds(feeds: List<FeedEntity>) {
            insertedFeeds.addAll(elements = feeds)
        }
    }

    private fun createRepository(
        dao: FakeSourceDao,
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
        )
    }

    @Test
    fun insert_mapsToEntityAndCallsDao() = runTest {
        val dao = FakeSourceDao()
        val repository = createRepository(dao = dao)
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
            actual = dao.insertedSources.size,
        )
        val entity = dao.insertedSources.first()
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

    @Test
    fun fetchAndSave_fetchesRssAndInsertsSourceWithFeeds() = runTest {
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
        val repository = createRepository(dao = dao, xmlResponse = xml)

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
    }
}
