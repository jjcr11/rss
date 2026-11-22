package com.jjcr11.rss.data.remote.mapper

import com.jjcr11.rss.data.remote.dto.SourceDto
import com.jjcr11.rss.domain.model.Source
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.time.Instant
import kotlin.uuid.Uuid

class SourceDtoMapperTest {

    @Test
    fun toDomain_mapsSourceDtoToDomainCorrectly() {
        val dto = SourceDto(
            id = "123e4567-e89b-12d3-a456-426614174000",
            rssUrl = "https://example.com/rss",
            webUrl = "https://example.com",
            name = "Example News",
            createdAt = 1000L,
            updatedAt = 2000L,
        )

        val domain = dto.toDomain()

        assertEquals(
            expected = Uuid.parse("123e4567-e89b-12d3-a456-426614174000"),
            actual = domain.id,
        )
        assertEquals(
            expected = "https://example.com/rss",
            actual = domain.rssUrl,
        )
        assertEquals(
            expected = "https://example.com",
            actual = domain.webUrl,
        )
        assertEquals(
            expected = "Example News",
            actual = domain.name,
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
    fun toDto_mapsDomainToSourceDtoCorrectly() {
        val domain = Source(
            id = Uuid.parse("123e4567-e89b-12d3-a456-426614174000"),
            rssUrl = "https://example.com/rss",
            webUrl = "https://example.com",
            name = "Example News",
            createdAt = Instant.fromEpochMilliseconds(epochMilliseconds = 1000L),
            updatedAt = Instant.fromEpochMilliseconds(epochMilliseconds = 2000L),
        )

        val dto = domain.toDto()

        assertEquals(
            expected = "123e4567-e89b-12d3-a456-426614174000",
            actual = dto.id,
        )
        assertEquals(
            expected = "https://example.com/rss",
            actual = dto.rssUrl,
        )
        assertEquals(
            expected = "https://example.com",
            actual = dto.webUrl,
        )
        assertEquals(
            expected = "Example News",
            actual = dto.name,
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
