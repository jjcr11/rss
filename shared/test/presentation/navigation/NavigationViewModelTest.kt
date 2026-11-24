package com.jjcr11.rss.presentation.navigation

import com.jjcr11.rss.domain.manager.SyncManager
import com.jjcr11.rss.domain.model.AuthState
import com.jjcr11.rss.domain.model.Feed
import com.jjcr11.rss.domain.model.Source
import com.jjcr11.rss.domain.model.User
import com.jjcr11.rss.domain.repository.AuthRepository
import com.jjcr11.rss.domain.repository.FeedRepository
import com.jjcr11.rss.domain.repository.PreferencesRepository
import com.jjcr11.rss.domain.repository.SourceRepository
import com.jjcr11.rss.domain.usecase.ObserveAuthStateUseCase
import com.jjcr11.rss.domain.usecase.ObserveGuestModeUseCase
import com.jjcr11.rss.domain.usecase.SyncFeedsUseCase
import com.jjcr11.rss.domain.usecase.SyncSourcesUseCase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.uuid.Uuid

@OptIn(ExperimentalCoroutinesApi::class)
class NavigationViewModelTest {

    private val testDispatcher = StandardTestDispatcher()

    private class FakeAuthRepository : AuthRepository {
        val authStateFlow = MutableStateFlow<AuthState>(value = AuthState.Initializing)

        override val authState: MutableStateFlow<AuthState> = authStateFlow

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

    private class FakeSourceRepository : SourceRepository {
        override suspend fun fetchAndSave(url: String): Source = throw NotImplementedError()
        override suspend fun syncSources() = Unit
        override suspend fun listenToRealtimeSources() = Unit
        override fun getAll(): Flow<List<Source>> = flowOf(value = emptyList())
        override suspend fun delete(source: Source): Boolean = true
    }

    private class FakeFeedRepository : FeedRepository {
        override fun getAll(): Flow<List<Feed>> = flowOf(value = emptyList())
        override suspend fun syncFeeds() = Unit
        override suspend fun setAsRead(id: Uuid) = Unit
        override suspend fun fetchFeedsFromSources() = Unit
    }

    private class FakeSyncManager : SyncManager(
        syncSourcesUseCase = SyncSourcesUseCase(repository = FakeSourceRepository()),
        syncFeedsUseCase = SyncFeedsUseCase(repository = FakeFeedRepository()),
    ) {
        var syncCalled = false
        private val _isSyncing = MutableStateFlow(value = false)
        override val isSyncing: StateFlow<Boolean> = _isSyncing.asStateFlow()

        override suspend fun sync() {
            syncCalled = true
        }
    }

    private class FakePreferencesRepository : PreferencesRepository {
        val guestModeFlow = MutableStateFlow(value = false)
        override val isGuestMode: Flow<Boolean> = guestModeFlow
        override suspend fun setGuestMode(isGuestMode: Boolean) {
            guestModeFlow.value = isGuestMode
        }
    }

    private lateinit var repository: FakeAuthRepository
    private lateinit var preferencesRepository: FakePreferencesRepository
    private lateinit var syncManager: FakeSyncManager
    private lateinit var observeAuthStateUseCase: ObserveAuthStateUseCase
    private lateinit var observeGuestModeUseCase: ObserveGuestModeUseCase
    private lateinit var viewModel: NavigationViewModel

    @BeforeTest
    fun setUp() {
        Dispatchers.setMain(dispatcher = testDispatcher)
        repository = FakeAuthRepository()
        preferencesRepository = FakePreferencesRepository()
        syncManager = FakeSyncManager()
        observeAuthStateUseCase = ObserveAuthStateUseCase(repository = repository)
        observeGuestModeUseCase = ObserveGuestModeUseCase(repository = preferencesRepository)
        viewModel = NavigationViewModel(
            observeAuthStateUseCase = observeAuthStateUseCase,
            syncManager = syncManager,
            observeGuestModeUseCase = observeGuestModeUseCase,
        )
    }

    @AfterTest
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun initialState_isInitializing() {
        assertEquals(expected = AuthState.Initializing, actual = viewModel.authState.value)
    }

    @Test
    fun isGuestMode_whenEmitted_updatesState() = runTest {
        preferencesRepository.guestModeFlow.value = true
        advanceUntilIdle()

        assertEquals(expected = true, actual = viewModel.isGuestMode.value)
    }

    @Test
    fun authState_whenEmitsUnauthenticated_updatesState() = runTest {
        repository.authStateFlow.value = AuthState.Unauthenticated
        advanceUntilIdle()

        assertEquals(expected = AuthState.Unauthenticated, actual = viewModel.authState.value)
        assertEquals(expected = false, actual = syncManager.syncCalled)
    }

    @Test
    fun authState_whenEmitsAuthenticated_updatesStateAndTriggersSync() = runTest {
        val user = User(
            id = Uuid.parse(uuidString = "00000000-0000-0000-0000-000000000001"),
            email = "user@test.com",
        )
        repository.authStateFlow.value = AuthState.Authenticated(user = user)
        advanceUntilIdle()

        assertEquals(
            expected = AuthState.Authenticated(user = user),
            actual = viewModel.authState.value,
        )
        assertEquals(expected = true, actual = syncManager.syncCalled)
    }

    @Test
    fun sync_callsSyncManagerSync() = runTest {
        viewModel.sync()
        advanceUntilIdle()

        assertEquals(expected = true, actual = syncManager.syncCalled)
    }
}
