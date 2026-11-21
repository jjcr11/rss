package com.jjcr11.rss.data.remote.datasource

import com.jjcr11.rss.data.remote.parser.RssXmlParser
import io.ktor.client.HttpClient
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.respond
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.http.headersOf
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals

class RssRemoteDataSourceTest {

    private val sampleRssXml = """
        <?xml version="1.0" encoding="UTF-8"?>
        <rss version="2.0">
            <channel>
                <title>Mock Tech Blog</title>
                <link>https://example.com/</link>
                <description>Mock blog description</description>
                <item>
                    <title>Mock Article</title>
                    <link>https://example.com/posts/1</link>
                </item>
            </channel>
        </rss>
    """.trimIndent()

    @Test
    fun fetchRss_fetchesXmlAndParsesSuccessfully() = runTest {
        val mockEngine = MockEngine { request ->
            assertEquals(
                expected = "https://example.com/feed.xml",
                actual = request.url.toString(),
            )
            respond(
                content = sampleRssXml,
                status = HttpStatusCode.OK,
                headers = headersOf(
                    name = HttpHeaders.ContentType,
                    value = "application/rss+xml; charset=utf-8",
                ),
            )
        }

        val httpClient = HttpClient(engine = mockEngine)
        val dataSource = RssRemoteDataSource(
            httpClient = httpClient,
            rssXmlParser = RssXmlParser(),
        )

        val result = dataSource.fetchRss(url = "https://example.com/feed.xml")

        assertEquals(
            expected = "Mock Tech Blog",
            actual = result.title,
        )
        assertEquals(
            expected = "https://example.com/",
            actual = result.link,
        )
        assertEquals(
            expected = 1,
            actual = result.feeds.size,
        )
        assertEquals(
            expected = "Mock Article",
            actual = result.feeds.first().title,
        )
        assertEquals(
            expected = "https://example.com/posts/1",
            actual = result.feeds.first().link,
        )
    }
}
