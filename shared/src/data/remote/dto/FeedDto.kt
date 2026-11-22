package com.jjcr11.rss.data.remote.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class FeedDto(
    @SerialName("id")
    val id: String,
    @SerialName("source_id")
    val sourceId: String,
    @SerialName("title")
    val title: String,
    @SerialName("read")
    val read: Boolean,
    @SerialName("save")
    val save: Boolean,
    @SerialName("url")
    val url: String,
    @SerialName("date")
    val date: Long,
    @SerialName("created_at")
    val createdAt: Long,
    @SerialName("updated_at")
    val updatedAt: Long,
)
