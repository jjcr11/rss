package com.jjcr11.rss.data.remote.mapper

import com.jjcr11.rss.data.remote.dto.FeedXmlDto
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.time.Instant
import kotlin.uuid.Uuid

class FeedXmlMapperTest {

    @Test
    fun toDomain_mapsFeedXmlDtoToDomainCorrectly() {
        val sourceId = Uuid.parse(uuidString = "123e4567-e89b-12d3-a456-426614174000")
        val fixedFeedId = Uuid.parse(uuidString = "a1b2c3d4-e5f6-7890-abcd-ef1234567890")
        val fixedNow = Instant.parse(input = "2026-11-21T15:47:17Z")
        val fixedItemDate = Instant.parse(input = "2026-11-21T13:00:31Z")

        val feedXmlDto = FeedXmlDto(
            title = "Introduction to Clean Architecture in Kotlin Multiplatform",
            link = "https://example.com/posts/clean-architecture-kmp",
            pubDate = "2026-11-21T13:00:31Z",
            description = "A comprehensive overview of Clean Architecture principles.",
            guid = "https://example.com/posts/clean-architecture-kmp#guid",
            isPermaLink = false,
            author = "Jane Doe",
            commentsUrl = "https://example.com/posts/clean-architecture-kmp/comments",
            commentCount = 0,
            categories = listOf("Architecture", "Kotlin", "KMP"),
            imageUrl = "https://example.com/images/kmp-architecture.jpg",
        )

        val domainFeed = feedXmlDto.toDomain(
            sourceId = sourceId,
            id = fixedFeedId,
            now = fixedNow,
            date = fixedItemDate,
        )

        assertEquals(
            expected = fixedFeedId,
            actual = domainFeed.id,
        )
        assertEquals(
            expected = sourceId,
            actual = domainFeed.sourceId,
        )
        assertEquals(
            expected = "Introduction to Clean Architecture in Kotlin Multiplatform",
            actual = domainFeed.title,
        )
        assertEquals(
            expected = "A comprehensive overview of Clean Architecture principles.",
            actual = domainFeed.content,
        )
        assertEquals(
            expected = "https://example.com/posts/clean-architecture-kmp",
            actual = domainFeed.url,
        )
        assertFalse(actual = domainFeed.read)
        assertFalse(actual = domainFeed.save)
        assertEquals(
            expected = fixedItemDate,
            actual = domainFeed.date,
        )
        assertEquals(
            expected = fixedNow,
            actual = domainFeed.createdAt,
        )
        assertEquals(
            expected = fixedNow,
            actual = domainFeed.updatedAt,
        )
    }

    @Test
    fun toDomain_whenDateIsInvalid_fallsBackToNow() {
        val sourceId = Uuid.parse(uuidString = "123e4567-e89b-12d3-a456-426614174000")
        val fixedFeedId = Uuid.parse(uuidString = "a1b2c3d4-e5f6-7890-abcd-ef1234567890")
        val fixedNow = Instant.parse(input = "2026-11-21T15:47:17Z")

        val feedXmlDto = FeedXmlDto(
            title = "Sample Item",
            link = "https://example.com/sample",
            pubDate = "invalid-date",
        )

        val domainFeed = feedXmlDto.toDomain(
            sourceId = sourceId,
            id = fixedFeedId,
            now = fixedNow,
        )

        assertEquals(
            expected = fixedNow,
            actual = domainFeed.date,
        )
        assertEquals(
            expected = fixedNow,
            actual = domainFeed.createdAt,
        )
        assertEquals(
            expected = fixedNow,
            actual = domainFeed.updatedAt,
        )
    }

    @Test
    fun toDomain_whenDateIsNull_fallsBackToNow() {
        val sourceId = Uuid.parse(uuidString = "123e4567-e89b-12d3-a456-426614174000")
        val fixedFeedId = Uuid.parse(uuidString = "a1b2c3d4-e5f6-7890-abcd-ef1234567890")
        val fixedNow = Instant.parse(input = "2026-11-21T15:47:17Z")

        val feedXmlDto = FeedXmlDto(
            title = "Sample Item",
            link = "https://example.com/sample",
            pubDate = null,
        )

        val domainFeed = feedXmlDto.toDomain(
            sourceId = sourceId,
            id = fixedFeedId,
            now = fixedNow,
        )

        assertEquals(
            expected = fixedNow,
            actual = domainFeed.date,
        )
    }

    @Test
    fun toDomain_whenContentIsProvided_prefersContentOverDescription() {
        val sourceId = Uuid.parse(uuidString = "123e4567-e89b-12d3-a456-426614174000")
        val fixedFeedId = Uuid.parse(uuidString = "a1b2c3d4-e5f6-7890-abcd-ef1234567890")
        val fixedNow = Instant.parse(input = "2026-11-21T15:47:17Z")

        val feedXmlDto = FeedXmlDto(
            title = "Sample Item",
            link = "https://example.com/sample",
            description = "Short summary",
            content = "<p>Full content body</p>",
        )

        val domainFeed = feedXmlDto.toDomain(
            sourceId = sourceId,
            id = fixedFeedId,
            now = fixedNow,
        )

        assertEquals(
            expected = "<p>Full content body</p>",
            actual = domainFeed.content,
        )
    }
}
