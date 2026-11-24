package com.jjcr11.rss.presentation.settingsscreen

import com.jjcr11.rss.domain.model.AuthState
import com.jjcr11.rss.domain.repository.AuthRepository
import com.jjcr11.rss.domain.usecase.LogoutUseCase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kotlin.coroutines.cancellation.CancellationException
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

@OptIn(ExperimentalCoroutinesApi::class)
class SettingsViewModelTest {

    private val testDispatcher = StandardTestDispatcher()

    private class FakeAuthRepository : AuthRepository {
        var logoutCalled = false
        var logoutShouldThrow: Exception? = null

        override val authState: Flow<AuthState> = emptyFlow()

        override suspend fun signUpWithEmail(email: String, password: String) = Unit

        override suspend fun signInWithEmail(email: String, password: String) = Unit

        override suspend fun authenticateWithEmail(email: String, password: String) = Unit

        override suspend fun logout() {
            logoutCalled = true
            logoutShouldThrow?.let { throw it }
        }
    }

    private lateinit var repository: FakeAuthRepository
    private lateinit var logoutUseCase: LogoutUseCase
    private lateinit var viewModel: SettingsViewModel

    @BeforeTest
    fun setUp() {
        Dispatchers.setMain(dispatcher = testDispatcher)
        repository = FakeAuthRepository()
        logoutUseCase = LogoutUseCase(repository = repository)
        viewModel = SettingsViewModel(logoutUseCase = logoutUseCase)
    }

    @AfterTest
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun initialState_hasDefaultValues() {
        val state = viewModel.uiState.value
        assertFalse(actual = state.isLoading)
        assertFalse(actual = state.isLoggedOut)
        assertNull(actual = state.errorMessage)
    }

    @Test
    fun onEvent_logout_whenSuccessful_updatesStateToLoggedOut() = runTest {
        viewModel.onEvent(event = SettingsUiEvent.Logout)
        advanceUntilIdle()

        assertTrue(actual = repository.logoutCalled)
        assertFalse(actual = viewModel.uiState.value.isLoading)
        assertTrue(actual = viewModel.uiState.value.isLoggedOut)
        assertNull(actual = viewModel.uiState.value.errorMessage)
    }

    @Test
    fun onEvent_logout_whenFails_setsErrorMessage() = runTest {
        repository.logoutShouldThrow = RuntimeException("Logout failed")

        viewModel.onEvent(event = SettingsUiEvent.Logout)
        advanceUntilIdle()

        assertTrue(actual = repository.logoutCalled)
        assertFalse(actual = viewModel.uiState.value.isLoading)
        assertFalse(actual = viewModel.uiState.value.isLoggedOut)
        assertEquals(
            expected = "Logout failed",
            actual = viewModel.uiState.value.errorMessage,
        )
    }

    @Test
    fun onEvent_logout_whenCancellationExceptionThrown_doesNotSetErrorMessage() = runTest {
        repository.logoutShouldThrow = CancellationException("Job was cancelled")

        viewModel.onEvent(event = SettingsUiEvent.Logout)
        advanceUntilIdle()

        assertFalse(actual = viewModel.uiState.value.isLoggedOut)
        assertNull(actual = viewModel.uiState.value.errorMessage)
    }
}
