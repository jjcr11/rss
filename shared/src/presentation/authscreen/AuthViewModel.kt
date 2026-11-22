package com.jjcr11.rss.presentation.authscreen

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.jjcr11.rss.domain.usecase.AuthenticateWithEmailUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlin.coroutines.cancellation.CancellationException

class AuthViewModel(
    private val authenticateWithEmailUseCase: AuthenticateWithEmailUseCase,
) : ViewModel() {

    private val _uiState = MutableStateFlow(value = AuthUiState())
    val uiState: StateFlow<AuthUiState> = _uiState.asStateFlow()

    fun onEvent(event: AuthUiEvent) {
        when (event) {
            is AuthUiEvent.AuthenticateWithEmail -> authenticateWithEmail(
                email = event.email,
                password = event.password,
            )
            AuthUiEvent.ClearError -> clearError()
        }
    }

    private fun authenticateWithEmail(email: String, password: String) {
        val trimmedEmail = email.trim()
        val trimmedPassword = password.trim()
        if (trimmedEmail.isBlank() || trimmedPassword.isBlank()) return

        viewModelScope.launch {
            _uiState.update { currentState ->
                currentState.copy(
                    isLoading = true,
                    errorMessage = null,
                )
            }
            try {
                authenticateWithEmailUseCase(
                    email = trimmedEmail,
                    password = trimmedPassword,
                )
                _uiState.update { currentState ->
                    currentState.copy(
                        isLoading = false,
                        isSuccess = true,
                        errorMessage = null,
                    )
                }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                _uiState.update { currentState ->
                    currentState.copy(
                        isLoading = false,
                        errorMessage = e.message,
                    )
                }
            }
        }
    }

    private fun clearError() {
        _uiState.update { currentState ->
            currentState.copy(errorMessage = null)
        }
    }
}
