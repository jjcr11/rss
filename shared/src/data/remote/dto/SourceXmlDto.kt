package com.jjcr11.rss.data.remote.dto

data class SourceXmlDto(
    val title: String,
    val link: String,
    val description: String? = null,
    val language: String? = null,
    val lastBuildDate: String? = null,
    val updatePeriod: String? = null,
    val updateFrequency: String? = null,
    val feeds: List<FeedXmlDto> = emptyList(),
)
