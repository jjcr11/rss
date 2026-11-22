package com.jjcr11.rss.presentation.authscreen

sealed interface AuthUiEvent {
    data class SignUpWithEmail(
        val email: String,
        val password: String,
    ) : AuthUiEvent

    data object ClearError : AuthUiEvent
}
