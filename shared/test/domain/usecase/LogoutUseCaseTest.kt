package com.jjcr11.rss.domain.usecase

import com.jjcr11.rss.domain.model.AuthState
import com.jjcr11.rss.domain.repository.AuthRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertTrue

class LogoutUseCaseTest {

    private class FakeAuthRepository : AuthRepository {
        var logoutCalled = false

        override val authState: Flow<AuthState> = emptyFlow()

        override suspend fun signUpWithEmail(email: String, password: String) = Unit

        override suspend fun signInWithEmail(email: String, password: String) = Unit

        override suspend fun authenticateWithEmail(email: String, password: String) = Unit

        override suspend fun logout() {
            logoutCalled = true
        }
    }

    @Test
    fun invoke_callsLogoutOnRepository() = runTest {
        val repository = FakeAuthRepository()
        val useCase = LogoutUseCase(repository = repository)

        useCase()

        assertTrue(actual = repository.logoutCalled)
    }
}
