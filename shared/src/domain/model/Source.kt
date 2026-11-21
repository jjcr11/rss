package com.jjcr11.rss.domain.model

import kotlin.time.Instant
import kotlin.uuid.Uuid

data class Source(
    val id: Uuid,
    val rssUrl: String,
    val webUrl: String,
    val name: String,
    val createdAt: Instant,
    val updatedAt: Instant,
)
