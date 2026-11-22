package com.jjcr11.rss.domain.model

import kotlin.time.Instant
import kotlin.uuid.Uuid

data class Feed(
    val id: Uuid,
    val sourceId: Uuid,
    val title: String,
    val content: String = "",
    val read: Boolean,
    val save: Boolean,
    val url: String,
    val date: Instant,
    val createdAt: Instant,
    val updatedAt: Instant,
)
