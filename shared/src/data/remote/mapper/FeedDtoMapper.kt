package com.jjcr11.rss.data.remote.mapper

import com.jjcr11.rss.data.remote.dto.FeedDto
import com.jjcr11.rss.domain.model.Feed
import kotlin.time.Instant
import kotlin.uuid.Uuid

fun FeedDto.toDomain(): Feed = Feed(
    id = Uuid.parse(uuidString = this.id),
    sourceId = Uuid.parse(uuidString = this.sourceId),
    title = this.title,
    content = this.content,
    isRead = this.isRead,
    isSaved = this.isSaved,
    url = this.url,
    date = Instant.fromEpochMilliseconds(epochMilliseconds = this.date),
    createdAt = Instant.fromEpochMilliseconds(epochMilliseconds = this.createdAt),
    updatedAt = Instant.fromEpochMilliseconds(epochMilliseconds = this.updatedAt),
)

fun Feed.toDto(): FeedDto = FeedDto(
    id = this.id.toString(),
    sourceId = this.sourceId.toString(),
    title = this.title,
    content = this.content,
    isRead = this.isRead,
    isSaved = this.isSaved,
    url = this.url,
    date = this.date.toEpochMilliseconds(),
    createdAt = this.createdAt.toEpochMilliseconds(),
    updatedAt = this.updatedAt.toEpochMilliseconds(),
)
