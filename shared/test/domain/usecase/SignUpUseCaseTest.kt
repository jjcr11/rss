package com.jjcr11.rss.domain.usecase

import com.jjcr11.rss.domain.model.AuthState
import com.jjcr11.rss.domain.repository.AuthRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals

class SignUpUseCaseTest {

    private class FakeAuthRepository : AuthRepository {
        var lastEmail: String? = null
        var lastPassword: String? = null

        override val authState: Flow<AuthState> = emptyFlow()

        override suspend fun signUpWithEmail(email: String, password: String) {
            lastEmail = email
            lastPassword = password
        }

        override suspend fun signInWithEmail(email: String, password: String) {
            throw NotImplementedError()
        }

        override suspend fun authenticateWithEmail(email: String, password: String) {
            throw NotImplementedError()
        }
    }

    @Test
    fun invoke_callsSignUpWithEmailOnRepository() = runTest {
        val repository = FakeAuthRepository()
        val useCase = SignUpUseCase(repository = repository)

        useCase(email = "test@example.com", password = "password123")

        assertEquals(
            expected = "test@example.com",
            actual = repository.lastEmail,
        )
        assertEquals(
            expected = "password123",
            actual = repository.lastPassword,
        )
    }
}
