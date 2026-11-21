package com.jjcr11.rss.presentation.addsourcescreen

sealed interface AddSourceUiEvent {
    data class AddFeed(
        val url: String,
    ) : AddSourceUiEvent

    data object ClearError : AddSourceUiEvent
}
