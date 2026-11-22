package com.jjcr11.rss.domain.usecase

import com.jjcr11.rss.domain.repository.AuthRepository

class SignUpUseCase(
    private val repository: AuthRepository,
) {
    suspend operator fun invoke(email: String, password: String) {
        repository.signUpWithEmail(email = email, password = password)
    }
}
