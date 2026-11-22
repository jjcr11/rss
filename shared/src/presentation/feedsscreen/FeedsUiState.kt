package com.jjcr11.rss.presentation.feedsscreen

data class FeedsUiState(
    val feeds: List<FeedUiModel> = emptyList(),
    val isLoading: Boolean = false,
)
