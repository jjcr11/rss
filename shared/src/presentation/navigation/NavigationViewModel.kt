package com.jjcr11.rss.presentation.navigation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.jjcr11.rss.domain.manager.SyncManager
import com.jjcr11.rss.domain.model.AuthState
import com.jjcr11.rss.domain.usecase.ObserveAuthStateUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class NavigationViewModel(
    private val observeAuthStateUseCase: ObserveAuthStateUseCase,
    private val syncManager: SyncManager,
) : ViewModel() {

    private val _authState = MutableStateFlow<AuthState>(value = AuthState.Initializing)
    val authState: StateFlow<AuthState> = _authState.asStateFlow()

    init {
        observeAuthState()
    }

    private fun observeAuthState() {
        viewModelScope.launch {
            observeAuthStateUseCase().collect { state ->
                _authState.value = state
                if (state is AuthState.Authenticated) {
                    syncManager.sync()
                }
            }
        }
    }

    fun sync() {
        viewModelScope.launch {
            syncManager.sync()
        }
    }
}
