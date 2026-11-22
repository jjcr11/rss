package com.jjcr11.rss.data.remote.mapper

import com.jjcr11.rss.data.remote.dto.FeedXmlDto
import com.jjcr11.rss.domain.model.Feed
import kotlin.time.Clock
import kotlin.time.Instant
import kotlin.uuid.Uuid

fun FeedXmlDto.toDomain(
    sourceId: Uuid,
    id: Uuid = Uuid.random(),
    now: Instant = Clock.System.now(),
    date: Instant = parseRssDate(
        dateString = this.pubDate,
        fallback = now,
    ),
): Feed = Feed(
    id = id,
    sourceId = sourceId,
    title = this.title,
    content = this.content.ifBlank { this.description.orEmpty() },
    read = false,
    save = false,
    url = this.link,
    date = date,
    createdAt = now,
    updatedAt = now,
)

private fun parseRssDate(
    dateString: String?,
    fallback: Instant,
): Instant {
    if (dateString.isNullOrBlank()) {
        return fallback
    }
    return try {
        Instant.parse(input = dateString)
    } catch (_: Exception) {
        fallback
    }
}
