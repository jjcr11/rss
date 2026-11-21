package com.jjcr11.rss.presentation.addsourcescreen

data class AddSourceUiState(
    val sources: List<SourceUiModel> = emptyList(),
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
)
