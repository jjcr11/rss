package com.jjcr11.rss.presentation.navigation

import com.jjcr11.rss.domain.model.AuthState
import com.jjcr11.rss.domain.model.User
import com.jjcr11.rss.domain.repository.AuthRepository
import com.jjcr11.rss.domain.usecase.ObserveAuthStateUseCase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
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
    }

    private lateinit var repository: FakeAuthRepository
    private lateinit var observeAuthStateUseCase: ObserveAuthStateUseCase
    private lateinit var viewModel: NavigationViewModel

    @BeforeTest
    fun setUp() {
        Dispatchers.setMain(dispatcher = testDispatcher)
        repository = FakeAuthRepository()
        observeAuthStateUseCase = ObserveAuthStateUseCase(repository = repository)
        viewModel = NavigationViewModel(observeAuthStateUseCase = observeAuthStateUseCase)
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
    fun authState_whenEmitsUnauthenticated_updatesState() = runTest {
        repository.authStateFlow.value = AuthState.Unauthenticated
        advanceUntilIdle()

        assertEquals(expected = AuthState.Unauthenticated, actual = viewModel.authState.value)
    }

    @Test
    fun authState_whenEmitsAuthenticated_updatesState() = runTest {
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
    }
}
