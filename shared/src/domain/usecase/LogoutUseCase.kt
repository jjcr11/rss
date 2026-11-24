package com.jjcr11.rss.domain.usecase

import com.jjcr11.rss.domain.repository.AuthRepository

class LogoutUseCase(
    private val repository: AuthRepository,
) {
    suspend operator fun invoke() {
        repository.logout()
    }
}
