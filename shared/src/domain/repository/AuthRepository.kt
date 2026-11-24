package com.jjcr11.rss.domain.repository

import com.jjcr11.rss.domain.model.AuthState
import kotlinx.coroutines.flow.Flow

interface AuthRepository {
    val authState: Flow<AuthState>

    suspend fun signUpWithEmail(email: String, password: String)

    suspend fun signInWithEmail(email: String, password: String)

    suspend fun authenticateWithEmail(email: String, password: String)

    suspend fun logout()
}
