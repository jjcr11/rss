package com.jjcr11.rss.domain.model

sealed interface AuthState {
    data object Initializing : AuthState
    data object Unauthenticated : AuthState
    data class Authenticated(val user: User) : AuthState
}
