package com.jjcr11.rss.presentation.feeddetail

import com.jjcr11.rss.presentation.feedsscreen.FeedUiModel

data class FeedDetailUiState(
    val feeds: List<FeedUiModel> = emptyList(),
    val initialIndex: Int = 0,
    val isLoading: Boolean = true,
)
