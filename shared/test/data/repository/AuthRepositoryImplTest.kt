package com.jjcr11.rss.data.repository

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import com.jjcr11.rss.data.local.dao.SourceDao
import com.jjcr11.rss.data.local.entity.FeedEntity
import com.jjcr11.rss.data.local.entity.PendingDeletedSourceEntity
import com.jjcr11.rss.data.local.entity.SourceEntity
import com.jjcr11.rss.data.local.preferences.UserPreferencesDataSource
import com.jjcr11.rss.data.local.preferences.createDataStore
import com.jjcr11.rss.data.remote.datasource.AuthRemoteDataSource
import com.jjcr11.rss.domain.model.AuthState
import com.jjcr11.rss.domain.model.User
import io.github.jan.supabase.auth.status.SessionStatus
import io.github.jan.supabase.auth.user.UserInfo
import io.github.jan.supabase.auth.user.UserSession
import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.exceptions.RestException
import io.ktor.client.HttpClient
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.respond
import io.ktor.client.request.get
import io.ktor.http.HttpStatusCode
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse
import kotlin.test.assertSame
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
        var signInEmail: String? = null
        var signInPassword: String? = null
        var signInThrows: Exception? = null
        var signUpThrows: Exception? = null
        var signInCallCount = 0
        var signUpCallCount = 0
        var signOutCallCount = 0
        var signOutThrows: Exception? = null

        override val sessionStatus = sessionStatusFlow

        override suspend fun signUp(email: String, password: String) {
            signUpCallCount++
            lastEmail = email
            lastPassword = password
            signUpThrows?.let { throw it }
        }

        override suspend fun signIn(email: String, password: String) {
            signInCallCount++
            signInEmail = email
            signInPassword = password
            signInThrows?.let { throw it }
        }

        override suspend fun signOut() {
            signOutCallCount++
            signOutThrows?.let { throw it }
        }
    }

    private suspend fun createFakeRestException(
        statusCode: HttpStatusCode,
        error: String,
        description: String?,
    ): RestException {
        val client = HttpClient(
            engine = MockEngine {
                respond(
                    content = """{"error":"$error","error_description":"$description"}""",
                    status = statusCode,
                )
            },
        )
        val response = client.get(urlString = "https://example.com")
        return RestException(
            error = error,
            description = description,
            response = response,
        )
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
    fun signInWithEmail_delegatesToAuthRemoteDataSource() = runTest {
        val dataSource = FakeAuthRemoteDataSource()
        val repository = AuthRepositoryImpl(authRemoteDataSource = dataSource)

        repository.signInWithEmail(email = "test@example.com", password = "secretPassword")

        assertEquals(
            expected = "test@example.com",
            actual = dataSource.signInEmail,
        )
        assertEquals(
            expected = "secretPassword",
            actual = dataSource.signInPassword,
        )
    }

    @Test
    fun authenticateWithEmail_whenSignInSucceeds_doesNotCallSignUp() = runTest {
        val dataSource = FakeAuthRemoteDataSource()
        val repository = AuthRepositoryImpl(authRemoteDataSource = dataSource)

        repository.authenticateWithEmail(email = "user@example.com", password = "password123")

        assertEquals(
            expected = 1,
            actual = dataSource.signInCallCount,
        )
        assertEquals(
            expected = 0,
            actual = dataSource.signUpCallCount,
        )
        assertEquals(
            expected = "user@example.com",
            actual = dataSource.signInEmail,
        )
    }

    @Test
    fun authenticateWithEmail_whenSignInFailsWithInvalidCredentials_andSignUpSucceeds_completesSuccessfully() = runTest {
        val dataSource = FakeAuthRemoteDataSource()
        val repository = AuthRepositoryImpl(authRemoteDataSource = dataSource)

        dataSource.signInThrows = createFakeRestException(
            statusCode = HttpStatusCode.BadRequest,
            error = "invalid_grant",
            description = "Invalid login credentials",
        )

        repository.authenticateWithEmail(email = "newuser@example.com", password = "password123")

        assertEquals(
            expected = 1,
            actual = dataSource.signInCallCount,
        )
        assertEquals(
            expected = 1,
            actual = dataSource.signUpCallCount,
        )
        assertEquals(
            expected = "newuser@example.com",
            actual = dataSource.lastEmail,
        )
    }

    @Test
    fun authenticateWithEmail_whenSignInFailsWithInvalidCredentials_andSignUpFailsWithUserAlreadyExists_throwsSignInException() = runTest {
        val dataSource = FakeAuthRemoteDataSource()
        val repository = AuthRepositoryImpl(authRemoteDataSource = dataSource)

        val signInError = createFakeRestException(
            statusCode = HttpStatusCode.BadRequest,
            error = "invalid_grant",
            description = "Invalid login credentials",
        )
        val signUpError = createFakeRestException(
            statusCode = HttpStatusCode.UnprocessableEntity,
            error = "user_already_exists",
            description = "User already registered",
        )

        dataSource.signInThrows = signInError
        dataSource.signUpThrows = signUpError

        val thrown = assertFailsWith<RestException> {
            repository.authenticateWithEmail(email = "existing@example.com", password = "wrongPassword")
        }

        assertSame(
            expected = signInError,
            actual = thrown,
        )
    }

    @Test
    fun authenticateWithEmail_whenSignInFailsWithInvalidCredentials_andSignUpFailsWithOtherError_throwsSignUpException() = runTest {
        val dataSource = FakeAuthRemoteDataSource()
        val repository = AuthRepositoryImpl(authRemoteDataSource = dataSource)

        val signInError = createFakeRestException(
            statusCode = HttpStatusCode.BadRequest,
            error = "invalid_grant",
            description = "Invalid login credentials",
        )
        val signUpError = createFakeRestException(
            statusCode = HttpStatusCode.UnprocessableEntity,
            error = "weak_password",
            description = "Password should be at least 6 characters",
        )

        dataSource.signInThrows = signInError
        dataSource.signUpThrows = signUpError

        val thrown = assertFailsWith<RestException> {
            repository.authenticateWithEmail(email = "newuser@example.com", password = "123")
        }

        assertSame(
            expected = signUpError,
            actual = thrown,
        )
    }

    @Test
    fun authenticateWithEmail_whenSignInFailsWithOtherError_throwsImmediatelyWithoutCallingSignUp() = runTest {
        val dataSource = FakeAuthRemoteDataSource()
        val repository = AuthRepositoryImpl(authRemoteDataSource = dataSource)

        val networkError = RuntimeException("Connection failed")
        dataSource.signInThrows = networkError

        val thrown = assertFailsWith<RuntimeException> {
            repository.authenticateWithEmail(email = "user@example.com", password = "password123")
        }

        assertSame(
            expected = networkError,
            actual = thrown,
        )
        assertEquals(
            expected = 1,
            actual = dataSource.signInCallCount,
        )
        assertEquals(
            expected = 0,
            actual = dataSource.signUpCallCount,
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

    private class FakeSourceDao : SourceDao {
        var clearAllCalled = false
        override suspend fun insert(source: SourceEntity) = Unit
        override suspend fun insert(sources: List<SourceEntity>) = Unit
        override suspend fun insertFeeds(feeds: List<FeedEntity>) = Unit
        override fun getAll(): Flow<List<SourceEntity>> = emptyFlow()
        override suspend fun delete(source: SourceEntity): Int = 1
        override suspend fun deleteFeedsBySourceId(sourceId: String): Int = 0
        override suspend fun deleteSourceById(id: String): Int = 1
        override suspend fun deleteById(id: String): Int = 1
        override suspend fun insertPendingDeletedSource(pendingDeletedSource: PendingDeletedSourceEntity) = Unit
        override suspend fun getPendingDeletedSourceIds(): List<String> = emptyList()
        override suspend fun deletePendingDeletedSource(pendingDeletedSource: PendingDeletedSourceEntity): Int = 1
        override suspend fun deletePendingDeletedSourcesById(ids: List<String>): Int = ids.size
        override suspend fun deleteAll(): Int = 0
        override suspend fun deleteAllPendingDeleted(): Int = 0
        override suspend fun deleteAllFeeds(): Int = 0
        override suspend fun clearAll() {
            clearAllCalled = true
        }
    }

    @Test
    fun logout_signsOutRemoteAndClearsLocalDatabaseAndPreferences() = runTest {
        val authRemoteDataSource = FakeAuthRemoteDataSource()
        val sourceDao = FakeSourceDao()
        val dataStore: DataStore<Preferences> = createDataStore(
            producePath = { "/tmp/test_pref_${Uuid.random()}.preferences_pb" },
        )
        val userPreferencesDataSource = UserPreferencesDataSource(dataStore = dataStore)
        userPreferencesDataSource.setGuestMode(isGuestMode = true)

        val repository = AuthRepositoryImpl(
            authRemoteDataSource = authRemoteDataSource,
            sourceDao = sourceDao,
            userPreferencesDataSource = userPreferencesDataSource,
        )

        repository.logout()

        assertEquals(expected = 1, actual = authRemoteDataSource.signOutCallCount)
        assertTrue(actual = sourceDao.clearAllCalled)
        assertFalse(actual = userPreferencesDataSource.isGuestMode.first())
    }

    @Test
    fun logout_whenRemoteSignOutThrows_stillClearsLocalDatabaseAndPreferences() = runTest {
        val authRemoteDataSource = FakeAuthRemoteDataSource().apply {
            signOutThrows = RuntimeException("Network error on logout")
        }
        val sourceDao = FakeSourceDao()
        val dataStore: DataStore<Preferences> = createDataStore(
            producePath = { "/tmp/test_pref_${Uuid.random()}.preferences_pb" },
        )
        val userPreferencesDataSource = UserPreferencesDataSource(dataStore = dataStore)
        userPreferencesDataSource.setGuestMode(isGuestMode = true)

        val repository = AuthRepositoryImpl(
            authRemoteDataSource = authRemoteDataSource,
            sourceDao = sourceDao,
            userPreferencesDataSource = userPreferencesDataSource,
        )

        repository.logout()

        assertEquals(expected = 1, actual = authRemoteDataSource.signOutCallCount)
        assertTrue(actual = sourceDao.clearAllCalled)
        assertFalse(actual = userPreferencesDataSource.isGuestMode.first())
    }
}
