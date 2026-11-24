package com.jjcr11.rss.presentation.settingsscreen

sealed interface SettingsUiEvent {
    data object Logout : SettingsUiEvent
}
