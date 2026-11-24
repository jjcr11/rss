package com.jjcr11.rss.presentation.settingsscreen

data class SettingsUiState(
    val isLoading: Boolean = false,
    val isLoggedOut: Boolean = false,
    val errorMessage: String? = null,
)
