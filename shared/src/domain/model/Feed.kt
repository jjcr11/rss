package com.jjcr11.rss.domain.model

import kotlin.time.Instant
import kotlin.uuid.Uuid

data class Feed(
    val id: Uuid,
    val sourceId: Uuid,
    val title: String,
    val content: String = "",
    val isRead: Boolean,
    val isSaved: Boolean,
    val url: String,
    val date: Instant,
    val createdAt: Instant,
    val updatedAt: Instant,
)
