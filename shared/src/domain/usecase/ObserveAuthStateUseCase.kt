package com.jjcr11.rss.domain.usecase

import com.jjcr11.rss.domain.model.AuthState
import com.jjcr11.rss.domain.repository.AuthRepository
import kotlinx.coroutines.flow.Flow

class ObserveAuthStateUseCase(
    private val repository: AuthRepository,
) {
    operator fun invoke(): Flow<AuthState> = repository.authState
}
