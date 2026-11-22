package com.jjcr11.rss.data.remote

import com.jjcr11.rss.data.remote.dto.FeedDto
import com.jjcr11.rss.data.remote.dto.SourceDto
import io.github.jan.supabase.realtime.PostgresAction
import io.github.jan.supabase.realtime.decodeRecord
import io.github.jan.supabase.serializer.KotlinXSerializer
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.time.Instant

class SupabaseSerializationTest {

    private val json = Json {
        ignoreUnknownKeys = true
    }

    @Test
    fun decodePostgrestSourcesResponse_parsesListCorrectly() {
        val rawJson = """
            [
                {
                    "id": "e5c89a02-234b-4892-98e1-95c55be027a1",
                    "rss_url": "https://news.ycombinator.com/rss",
                    "web_url": "https://news.ycombinator.com",
                    "name": "Hacker News",
                    "created_at": 1700000000000,
                    "updated_at": 1700000000000
                }
            ]
        """.trimIndent()

        val sources = json.decodeFromString<List<SourceDto>>(string = rawJson)

        assertEquals(expected = 1, actual = sources.size)
        val (source) = sources
        assertEquals(expected = "e5c89a02-234b-4892-98e1-95c55be027a1", actual = source.id)
        assertEquals(expected = "https://news.ycombinator.com/rss", actual = source.rssUrl)
        assertEquals(expected = "https://news.ycombinator.com", actual = source.webUrl)
        assertEquals(expected = "Hacker News", actual = source.name)
        assertEquals(expected = 1700000000000L, actual = source.createdAt)
        assertEquals(expected = 1700000000000L, actual = source.updatedAt)
    }

    @Test
    fun decodePostgrestFeedsResponse_parsesListCorrectly() {
        val rawJson = """
            [
                {
                    "id": "feed-uuid-1",
                    "source_id": "e5c89a02-234b-4892-98e1-95c55be027a1",
                    "title": "Show HN: My New Project",
                    "read": false,
                    "save": false,
                    "url": "https://news.ycombinator.com/item?id=123",
                    "date": 1700000000000,
                    "created_at": 1700000000000,
                    "updated_at": 1700000000000
                }
            ]
        """.trimIndent()

        val feeds = json.decodeFromString<List<FeedDto>>(string = rawJson)

        assertEquals(expected = 1, actual = feeds.size)
        val (feed) = feeds
        assertEquals(expected = "feed-uuid-1", actual = feed.id)
        assertEquals(expected = "e5c89a02-234b-4892-98e1-95c55be027a1", actual = feed.sourceId)
        assertEquals(expected = "Show HN: My New Project", actual = feed.title)
        assertEquals(expected = "", actual = feed.content)
        assertFalse(actual = feed.read)
        assertFalse(actual = feed.save)
        assertEquals(expected = "https://news.ycombinator.com/item?id=123", actual = feed.url)
        assertEquals(expected = 1700000000000L, actual = feed.date)
    }

    @Test
    fun decodePostgrestFeedsResponse_withContent_parsesContentCorrectly() {
        val rawJson = """
            [
                {
                    "id": "feed-uuid-2",
                    "source_id": "e5c89a02-234b-4892-98e1-95c55be027a1",
                    "title": "Article Title",
                    "content": "<p>Full content</p>",
                    "read": true,
                    "save": false,
                    "url": "https://example.com/article",
                    "date": 1700000000000,
                    "created_at": 1700000000000,
                    "updated_at": 1700000000000
                }
            ]
        """.trimIndent()

        val feeds = json.decodeFromString<List<FeedDto>>(string = rawJson)
        val (feed) = feeds
        assertEquals(expected = "<p>Full content</p>", actual = feed.content)
    }

    @Test
    fun decodeRealtimePostgresActionInsert_decodesRecordCorrectlyWithoutLinkageErrors() {
        val jsonRecord = buildJsonObject {
            put(key = "id", value = "e5c89a02-234b-4892-98e1-95c55be027a1")
            put(key = "rss_url", value = "https://news.ycombinator.com/rss")
            put(key = "web_url", value = "https://news.ycombinator.com")
            put(key = "name", value = "Hacker News")
            put(key = "created_at", value = 1700000000000L)
            put(key = "updated_at", value = 1700000000000L)
        }
        val action = PostgresAction.Insert(
            record = jsonRecord,
            columns = emptyList(),
            commitTimestamp = Instant.fromEpochMilliseconds(epochMilliseconds = 1700000000000L),
            serializer = KotlinXSerializer(json = json),
        )

        val decodedSource = action.decodeRecord<SourceDto>()

        assertEquals(expected = "e5c89a02-234b-4892-98e1-95c55be027a1", actual = decodedSource.id)
        assertEquals(expected = "Hacker News", actual = decodedSource.name)
        assertEquals(expected = "https://news.ycombinator.com/rss", actual = decodedSource.rssUrl)
        assertEquals(expected = "https://news.ycombinator.com", actual = decodedSource.webUrl)
        assertEquals(expected = 1700000000000L, actual = decodedSource.createdAt)
        assertEquals(expected = 1700000000000L, actual = decodedSource.updatedAt)
    }
}
