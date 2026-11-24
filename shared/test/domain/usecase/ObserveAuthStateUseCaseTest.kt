package com.jjcr11.rss.domain.usecase

import com.jjcr11.rss.domain.model.AuthState
import com.jjcr11.rss.domain.model.User
import com.jjcr11.rss.domain.repository.AuthRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.uuid.Uuid

class ObserveAuthStateUseCaseTest {

    private class FakeAuthRepository(
        override val authState: Flow<AuthState>,
    ) : AuthRepository {
        override suspend fun signUpWithEmail(email: String, password: String) {
            throw NotImplementedError()
        }

        override suspend fun signInWithEmail(email: String, password: String) {
            throw NotImplementedError()
        }

        override suspend fun authenticateWithEmail(email: String, password: String) {
            throw NotImplementedError()
        }

        override suspend fun logout() = Unit
    }

    @Test
    fun invoke_returnsAuthStateFlowFromRepository() = runTest {
        val expectedUser = User(
            id = Uuid.parse(uuidString = "e5c89a02-234b-4892-98e1-95c55be027a1"),
            email = "user@example.com",
        )
        val expectedState = AuthState.Authenticated(user = expectedUser)
        val repository = FakeAuthRepository(authState = flowOf(expectedState))
        val useCase = ObserveAuthStateUseCase(repository = repository)

        val result = useCase().first()

        assertEquals(
            expected = expectedState,
            actual = result,
        )
    }
}
