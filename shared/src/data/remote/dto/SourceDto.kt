package com.jjcr11.rss.data.remote.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class SourceDto(
    @SerialName("id")
    val id: String,
    @SerialName("rss_url")
    val rssUrl: String,
    @SerialName("web_url")
    val webUrl: String,
    @SerialName("name")
    val name: String,
    @SerialName("created_at")
    val createdAt: Long,
    @SerialName("updated_at")
    val updatedAt: Long,
)
