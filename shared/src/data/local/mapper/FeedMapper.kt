package com.jjcr11.rss.data.local.mapper

import com.jjcr11.rss.data.local.entity.FeedEntity
import com.jjcr11.rss.domain.model.Feed
import kotlin.time.Instant
import kotlin.uuid.Uuid

fun FeedEntity.toDomain(): Feed = Feed(
    id = Uuid.parse(this.id),
    sourceId = Uuid.parse(this.sourceId),
    title = this.title,
    content = this.content,
    read = this.read,
    save = this.save,
    url = this.url,
    date = Instant.fromEpochMilliseconds(this.date),
    createdAt = Instant.fromEpochMilliseconds(this.createdAt),
    updatedAt = Instant.fromEpochMilliseconds(this.updatedAt),
)

fun Feed.toEntity(): FeedEntity = FeedEntity(
    id = this.id.toString(),
    sourceId = this.sourceId.toString(),
    title = this.title,
    content = this.content,
    read = this.read,
    save = this.save,
    url = this.url,
    date = this.date.toEpochMilliseconds(),
    createdAt = this.createdAt.toEpochMilliseconds(),
    updatedAt = this.updatedAt.toEpochMilliseconds(),
)
