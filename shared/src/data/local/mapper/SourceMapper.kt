package com.jjcr11.rss.data.local.mapper

import com.jjcr11.rss.data.local.entity.SourceEntity
import com.jjcr11.rss.domain.model.Source
import kotlin.time.Instant
import kotlin.uuid.Uuid

fun SourceEntity.toDomain(): Source = Source(
    id = Uuid.parse(this.id),
    rssUrl = this.rssUrl,
    webUrl = this.webUrl,
    name = this.name,
    createdAt = Instant.fromEpochMilliseconds(this.createdAt),
    updatedAt = Instant.fromEpochMilliseconds(this.updatedAt),
)

fun Source.toEntity(): SourceEntity = SourceEntity(
    id = this.id.toString(),
    rssUrl = this.rssUrl,
    webUrl = this.webUrl,
    name = this.name,
    createdAt = this.createdAt.toEpochMilliseconds(),
    updatedAt = this.updatedAt.toEpochMilliseconds(),
)
