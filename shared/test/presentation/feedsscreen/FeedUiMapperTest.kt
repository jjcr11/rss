package com.jjcr11.rss.presentation.feedsscreen

import com.jjcr11.rss.domain.model.Feed
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.time.Instant
import kotlin.uuid.Uuid

class FeedUiMapperTest {

    @Test
    fun toUiModel_mapsFieldsCorrectly() {
        val id = Uuid.random()
        val sourceId = Uuid.random()
        val instant = Instant.parse(input = "2024-11-04T12:00:00Z")
        val feed = Feed(
            id = id,
            sourceId = sourceId,
            title = "Test Title",
            read = false,
            save = false,
            url = "https://example.com/item",
            date = instant,
            createdAt = instant,
            updatedAt = instant,
        )

        val uiModel = feed.toUiModel(sourceName = "The Verge")

        assertEquals(
            expected = id.toString(),
            actual = uiModel.id,
        )
        assertEquals(
            expected = "The Verge",
            actual = uiModel.sourceName,
        )
        assertEquals(
            expected = "Test Title",
            actual = uiModel.title,
        )
        assertEquals(
            expected = "Nov 4, 2024",
            actual = uiModel.date,
        )
        assertEquals(
            expected = "https://example.com/item",
            actual = uiModel.url,
        )
    }

    @Test
    fun toDisplayDate_formatsCorrectly() {
        val instant = Instant.parse(input = "2025-01-15T08:30:00Z")
        val displayDate = instant.toDisplayDate()

        assertEquals(
            expected = "Jan 15, 2025",
            actual = displayDate,
        )
    }
}
