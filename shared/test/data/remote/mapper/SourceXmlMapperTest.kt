package com.jjcr11.rss.data.remote.mapper

import com.jjcr11.rss.data.remote.dto.SourceXmlDto
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.time.Instant
import kotlin.uuid.Uuid

class SourceXmlMapperTest {

    @Test
    fun toDomain_mapsSourceXmlDtoToDomainCorrectly() {
        val fixedId = Uuid.parse(uuidString = "123e4567-e89b-12d3-a456-426614174000")
        val fixedNow = Instant.parse(input = "2026-11-21T15:47:17Z")
        val rssUrl = "https://example.com/rss/feed"

        val sourceXmlDto = SourceXmlDto(
            title = "Tech News & Reviews",
            link = "https://example.com",
            description = "Daily tech news, software reviews, and guides.",
            language = "en-US",
            lastBuildDate = "Sat, 21 Nov 2026 15:47:17 +0000",
            updatePeriod = "hourly",
            updateFrequency = "1",
        )

        val domainSource = sourceXmlDto.toDomain(
            rssUrl = rssUrl,
            id = fixedId,
            now = fixedNow,
        )

        assertEquals(
            expected = fixedId,
            actual = domainSource.id,
        )
        assertEquals(
            expected = rssUrl,
            actual = domainSource.rssUrl,
        )
        assertEquals(
            expected = "https://example.com",
            actual = domainSource.webUrl,
        )
        assertEquals(
            expected = "Tech News & Reviews",
            actual = domainSource.name,
        )
        assertEquals(
            expected = fixedNow,
            actual = domainSource.createdAt,
        )
        assertEquals(
            expected = fixedNow,
            actual = domainSource.updatedAt,
        )
    }
}
