package com.jjcr11.rss.data.remote.dto

data class FeedXmlDto(
    val title: String,
    val link: String,
    val pubDate: String? = null,
    val description: String? = null,
    val guid: String? = null,
    val isPermaLink: Boolean = false,
    val author: String? = null,
    val commentsUrl: String? = null,
    val commentCount: Int? = null,
    val categories: List<String> = emptyList(),
    val imageUrl: String? = null,
)
