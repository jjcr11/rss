package com.jjcr11.rss.data.remote.mapper

import com.jjcr11.rss.data.remote.dto.SourceXmlDto
import com.jjcr11.rss.domain.model.Source
import kotlin.time.Clock
import kotlin.time.Instant
import kotlin.uuid.Uuid

fun SourceXmlDto.toDomain(
    rssUrl: String,
    id: Uuid = Uuid.random(),
    now: Instant = Clock.System.now(),
): Source = Source(
    id = id,
    rssUrl = rssUrl,
    webUrl = this.link,
    name = this.title,
    createdAt = now,
    updatedAt = now,
)
