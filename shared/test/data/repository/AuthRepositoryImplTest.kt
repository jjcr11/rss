package com.jjcr11.rss.data.repository

import com.jjcr11.rss.data.remote.datasource.AuthRemoteDataSource
import com.jjcr11.rss.domain.model.AuthState
import com.jjcr11.rss.domain.model.User
import io.github.jan.supabase.auth.status.SessionStatus
import io.github.jan.supabase.auth.user.UserInfo
import io.github.jan.supabase.auth.user.UserSession
import io.github.jan.supabase.createSupabaseClient
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import kotlin.uuid.Uuid

class AuthRepositoryImplTest {

    private class FakeAuthRemoteDataSource : AuthRemoteDataSource(
        supabaseClient = createSupabaseClient(
            supabaseUrl = "https://example.supabase.co",
            supabaseKey = "anon",
        ) {},
    ) {
        val sessionStatusFlow = MutableSharedFlow<SessionStatus>(replay = 1)
        var lastEmail: String? = null
        var lastPassword: String? = null

        override val sessionStatus = sessionStatusFlow

        override suspend fun signUp(email: String, password: String) {
            lastEmail = email
            lastPassword = password
        }
    }

    @Test
    fun signUpWithEmail_delegatesToAuthRemoteDataSource() = runTest {
        val dataSource = FakeAuthRemoteDataSource()
        val repository = AuthRepositoryImpl(authRemoteDataSource = dataSource)

        repository.signUpWithEmail(email = "test@example.com", password = "secretPassword")

        assertEquals(
            expected = "test@example.com",
            actual = dataSource.lastEmail,
        )
        assertEquals(
            expected = "secretPassword",
            actual = dataSource.lastPassword,
        )
    }

    @Test
    fun authState_whenInitializing_emitsInitializing() = runTest {
        val dataSource = FakeAuthRemoteDataSource()
        val repository = AuthRepositoryImpl(authRemoteDataSource = dataSource)

        dataSource.sessionStatusFlow.emit(value = SessionStatus.Initializing)

        val state = repository.authState.first()
        assertEquals(
            expected = AuthState.Initializing,
            actual = state,
        )
    }

    @Test
    fun authState_whenAuthenticatedWithUser_emitsAuthenticatedUser() = runTest {
        val dataSource = FakeAuthRemoteDataSource()
        val repository = AuthRepositoryImpl(authRemoteDataSource = dataSource)

        val userInfo = UserInfo(
            id = "e5c89a02-234b-4892-98e1-95c55be027a1",
            aud = "authenticated",
            email = "test@example.com",
        )
        val session = UserSession(
            accessToken = "access-token",
            refreshToken = "refresh-token",
            expiresIn = 3600L,
            tokenType = "bearer",
            user = userInfo,
        )
        dataSource.sessionStatusFlow.emit(value = SessionStatus.Authenticated(session = session))

        val state = repository.authState.first()
        assertTrue(actual = state is AuthState.Authenticated)
        val expectedUser = User(
            id = Uuid.parse(uuidString = "e5c89a02-234b-4892-98e1-95c55be027a1"),
            email = "test@example.com",
        )
        assertEquals(
            expected = expectedUser,
            actual = state.user,
        )
    }

    @Test
    fun authState_whenAuthenticatedWithNullUser_emitsUnauthenticated() = runTest {
        val dataSource = FakeAuthRemoteDataSource()
        val repository = AuthRepositoryImpl(authRemoteDataSource = dataSource)

        val session = UserSession(
            accessToken = "access-token",
            refreshToken = "refresh-token",
            expiresIn = 3600L,
            tokenType = "bearer",
            user = null,
        )
        dataSource.sessionStatusFlow.emit(value = SessionStatus.Authenticated(session = session))

        val state = repository.authState.first()
        assertEquals(
            expected = AuthState.Unauthenticated,
            actual = state,
        )
    }

    @Test
    fun authState_whenNotAuthenticated_emitsUnauthenticated() = runTest {
        val dataSource = FakeAuthRemoteDataSource()
        val repository = AuthRepositoryImpl(authRemoteDataSource = dataSource)

        dataSource.sessionStatusFlow.emit(value = SessionStatus.NotAuthenticated(isSignOut = true))

        val state = repository.authState.first()
        assertEquals(
            expected = AuthState.Unauthenticated,
            actual = state,
        )
    }
}
