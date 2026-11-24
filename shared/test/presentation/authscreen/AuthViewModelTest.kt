package com.jjcr11.rss.presentation.authscreen

import com.jjcr11.rss.domain.model.AuthState
import com.jjcr11.rss.domain.repository.AuthRepository
import com.jjcr11.rss.domain.repository.PreferencesRepository
import com.jjcr11.rss.domain.usecase.AuthenticateWithEmailUseCase
import com.jjcr11.rss.domain.usecase.SetGuestModeUseCase
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
class AuthViewModelTest {

    private val testDispatcher = StandardTestDispatcher()

    private class FakeAuthRepository : AuthRepository {
        var lastEmail: String? = null
        var lastPassword: String? = null
        var shouldThrow: Exception? = null

        override val authState: Flow<AuthState> = emptyFlow()

        override suspend fun signUpWithEmail(email: String, password: String) {
            throw NotImplementedError()
        }

        override suspend fun signInWithEmail(email: String, password: String) {
            throw NotImplementedError()
        }

        override suspend fun authenticateWithEmail(email: String, password: String) {
            lastEmail = email
            lastPassword = password
            shouldThrow?.let { throw it }
        }
    }

    private class FakePreferencesRepository : PreferencesRepository {
        var lastGuestMode: Boolean? = null
        override val isGuestMode: Flow<Boolean> = emptyFlow()
        override suspend fun setGuestMode(isGuestMode: Boolean) {
            lastGuestMode = isGuestMode
        }
    }

    private lateinit var repository: FakeAuthRepository
    private lateinit var preferencesRepository: FakePreferencesRepository
    private lateinit var authenticateWithEmailUseCase: AuthenticateWithEmailUseCase
    private lateinit var setGuestModeUseCase: SetGuestModeUseCase
    private lateinit var viewModel: AuthViewModel

    @BeforeTest
    fun setUp() {
        Dispatchers.setMain(dispatcher = testDispatcher)
        repository = FakeAuthRepository()
        preferencesRepository = FakePreferencesRepository()
        authenticateWithEmailUseCase = AuthenticateWithEmailUseCase(repository = repository)
        setGuestModeUseCase = SetGuestModeUseCase(repository = preferencesRepository)
        viewModel = AuthViewModel(
            authenticateWithEmailUseCase = authenticateWithEmailUseCase,
            setGuestModeUseCase = setGuestModeUseCase,
        )
    }

    @AfterTest
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun initialState_hasDefaultValues() {
        val state = viewModel.uiState.value

        assertFalse(actual = state.isLoading)
        assertFalse(actual = state.isSuccess)
        assertNull(actual = state.errorMessage)
    }

    @Test
    fun onEvent_authenticateWithEmail_whenBlank_doesNotCallUseCase() = runTest {
        viewModel.onEvent(
            event = AuthUiEvent.AuthenticateWithEmail(
                email = "   ",
                password = "password",
            ),
        )
        advanceUntilIdle()

        assertNull(actual = repository.lastEmail)
        assertFalse(actual = viewModel.uiState.value.isLoading)

        viewModel.onEvent(
            event = AuthUiEvent.AuthenticateWithEmail(
                email = "user@test.com",
                password = "   ",
            ),
        )
        advanceUntilIdle()

        assertNull(actual = repository.lastEmail)
    }

    @Test
    fun onEvent_authenticateWithEmail_whenSuccessful_updatesStateToSuccess() = runTest {
        viewModel.onEvent(
            event = AuthUiEvent.AuthenticateWithEmail(
                email = "  test@example.com  ",
                password = "  securePassword123  ",
            ),
        )
        advanceUntilIdle()

        assertEquals(
            expected = "test@example.com",
            actual = repository.lastEmail,
        )
        assertEquals(
            expected = "securePassword123",
            actual = repository.lastPassword,
        )
        assertFalse(actual = viewModel.uiState.value.isLoading)
        assertTrue(actual = viewModel.uiState.value.isSuccess)
        assertNull(actual = viewModel.uiState.value.errorMessage)
    }

    @Test
    fun onEvent_authenticateWithEmail_whenFails_setsErrorMessage() = runTest {
        repository.shouldThrow = RuntimeException("Email already in use")

        viewModel.onEvent(
            event = AuthUiEvent.AuthenticateWithEmail(
                email = "user@example.com",
                password = "password123",
            ),
        )
        advanceUntilIdle()

        assertFalse(actual = viewModel.uiState.value.isLoading)
        assertFalse(actual = viewModel.uiState.value.isSuccess)
        assertEquals(
            expected = "Email already in use",
            actual = viewModel.uiState.value.errorMessage,
        )
    }

    @Test
    fun onEvent_clearError_clearsErrorMessage() = runTest {
        repository.shouldThrow = RuntimeException("Some error")
        viewModel.onEvent(
            event = AuthUiEvent.AuthenticateWithEmail(
                email = "user@example.com",
                password = "password123",
            ),
        )
        advanceUntilIdle()

        assertEquals(
            expected = "Some error",
            actual = viewModel.uiState.value.errorMessage,
        )

        viewModel.onEvent(event = AuthUiEvent.ClearError)

        assertNull(actual = viewModel.uiState.value.errorMessage)
    }

    @Test
    fun onEvent_authenticateWithEmail_whenCancellationExceptionThrown_doesNotSetErrorMessage() = runTest {
        repository.shouldThrow = CancellationException("Job was cancelled")

        viewModel.onEvent(
            event = AuthUiEvent.AuthenticateWithEmail(
                email = "user@example.com",
                password = "password123",
            ),
        )
        advanceUntilIdle()

        assertFalse(actual = viewModel.uiState.value.isSuccess)
        assertNull(actual = viewModel.uiState.value.errorMessage)
    }

    @Test
    fun onEvent_continueWithoutAccount_setsGuestModeToTrue() = runTest {
        viewModel.onEvent(event = AuthUiEvent.ContinueWithoutAccount)
        advanceUntilIdle()

        assertEquals(
            expected = true,
            actual = preferencesRepository.lastGuestMode,
        )
    }

    @Test
    fun onEvent_authenticateWithEmail_whenSuccessful_setsGuestModeToFalse() = runTest {
        viewModel.onEvent(
            event = AuthUiEvent.AuthenticateWithEmail(
                email = "user@example.com",
                password = "password123",
            ),
        )
        advanceUntilIdle()

        assertTrue(actual = viewModel.uiState.value.isSuccess)
        assertEquals(
            expected = false,
            actual = preferencesRepository.lastGuestMode,
        )
    }
}
