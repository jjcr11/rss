package com.jjcr11.rss.data.remote.mapper

import com.jjcr11.rss.data.remote.dto.FeedDto
import com.jjcr11.rss.domain.model.Feed
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.time.Instant
import kotlin.uuid.Uuid

class FeedDtoMapperTest {

    @Test
    fun toDomain_mapsFeedDtoToDomainCorrectly() {
        val dto = FeedDto(
            id = "123e4567-e89b-12d3-a456-426614174000",
            sourceId = "223e4567-e89b-12d3-a456-426614174000",
            title = "Test Feed",
            content = "<p>Full content</p>",
            isRead = true,
            isSaved = false,
            url = "https://example.com/feed-1",
            date = 5000L,
            createdAt = 1000L,
            updatedAt = 2000L,
        )

        val domain = dto.toDomain()

        assertEquals(
            expected = Uuid.parse(uuidString = "123e4567-e89b-12d3-a456-426614174000"),
            actual = domain.id,
        )
        assertEquals(
            expected = Uuid.parse(uuidString = "223e4567-e89b-12d3-a456-426614174000"),
            actual = domain.sourceId,
        )
        assertEquals(
            expected = "Test Feed",
            actual = domain.title,
        )
        assertEquals(
            expected = "<p>Full content</p>",
            actual = domain.content,
        )
        assertEquals(
            expected = true,
            actual = domain.isRead,
        )
        assertEquals(
            expected = false,
            actual = domain.isSaved,
        )
        assertEquals(
            expected = "https://example.com/feed-1",
            actual = domain.url,
        )
        assertEquals(
            expected = Instant.fromEpochMilliseconds(epochMilliseconds = 5000L),
            actual = domain.date,
        )
        assertEquals(
            expected = Instant.fromEpochMilliseconds(epochMilliseconds = 1000L),
            actual = domain.createdAt,
        )
        assertEquals(
            expected = Instant.fromEpochMilliseconds(epochMilliseconds = 2000L),
            actual = domain.updatedAt,
        )
    }

    @Test
    fun toDto_mapsDomainToFeedDtoCorrectly() {
        val domain = Feed(
            id = Uuid.parse(uuidString = "123e4567-e89b-12d3-a456-426614174000"),
            sourceId = Uuid.parse(uuidString = "223e4567-e89b-12d3-a456-426614174000"),
            title = "Test Feed",
            content = "<p>Full content</p>",
            isRead = true,
            isSaved = false,
            url = "https://example.com/feed-1",
            date = Instant.fromEpochMilliseconds(epochMilliseconds = 5000L),
            createdAt = Instant.fromEpochMilliseconds(epochMilliseconds = 1000L),
            updatedAt = Instant.fromEpochMilliseconds(epochMilliseconds = 2000L),
        )

        val dto = domain.toDto()

        assertEquals(
            expected = "123e4567-e89b-12d3-a456-426614174000",
            actual = dto.id,
        )
        assertEquals(
            expected = "223e4567-e89b-12d3-a456-426614174000",
            actual = dto.sourceId,
        )
        assertEquals(
            expected = "Test Feed",
            actual = dto.title,
        )
        assertEquals(
            expected = "<p>Full content</p>",
            actual = dto.content,
        )
        assertEquals(
            expected = true,
            actual = dto.isRead,
        )
        assertEquals(
            expected = false,
            actual = dto.isSaved,
        )
        assertEquals(
            expected = "https://example.com/feed-1",
            actual = dto.url,
        )
        assertEquals(
            expected = 5000L,
            actual = dto.date,
        )
        assertEquals(
            expected = 1000L,
            actual = dto.createdAt,
        )
        assertEquals(
            expected = 2000L,
            actual = dto.updatedAt,
        )
    }
}
