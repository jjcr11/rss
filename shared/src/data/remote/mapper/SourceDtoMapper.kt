package com.jjcr11.rss.data.remote.mapper

import com.jjcr11.rss.data.remote.dto.SourceDto
import com.jjcr11.rss.domain.model.Source
import kotlin.time.Instant
import kotlin.uuid.Uuid

fun SourceDto.toDomain(): Source = Source(
    id = Uuid.parse(this.id),
    rssUrl = this.rssUrl,
    webUrl = this.webUrl,
    name = this.name,
    createdAt = Instant.fromEpochMilliseconds(epochMilliseconds = this.createdAt),
    updatedAt = Instant.fromEpochMilliseconds(epochMilliseconds = this.updatedAt),
)

fun Source.toDto(): SourceDto = SourceDto(
    id = this.id.toString(),
    rssUrl = this.rssUrl,
    webUrl = this.webUrl,
    name = this.name,
    createdAt = this.createdAt.toEpochMilliseconds(),
    updatedAt = this.updatedAt.toEpochMilliseconds(),
)
