package com.jjcr11.rss.presentation.feedsscreen

data class FeedUiModel(
    val id: String,
    val sourceName: String,
    val title: String,
    val date: String,
    val url: String,
    val content: String = "",
)
