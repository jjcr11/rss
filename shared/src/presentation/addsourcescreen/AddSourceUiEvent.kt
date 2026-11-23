package com.jjcr11.rss.presentation.addsourcescreen

sealed interface AddSourceUiEvent {
    data class AddFeed(
        val url: String,
    ) : AddSourceUiEvent

    data class DeleteSource(
        val source: SourceUiModel,
    ) : AddSourceUiEvent

    data object ClearError : AddSourceUiEvent
}
