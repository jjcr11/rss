package com.jjcr11.rss.presentation.navigation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.jjcr11.rss.domain.manager.SyncManager
import com.jjcr11.rss.domain.model.AuthState
import com.jjcr11.rss.domain.usecase.ObserveAuthStateUseCase
import com.jjcr11.rss.domain.usecase.ObserveGuestModeUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class NavigationViewModel(
    private val observeAuthStateUseCase: ObserveAuthStateUseCase,
    private val syncManager: SyncManager,
    private val observeGuestModeUseCase: ObserveGuestModeUseCase? = null,
) : ViewModel() {

    private val _authState = MutableStateFlow<AuthState>(value = AuthState.Initializing)
    val authState: StateFlow<AuthState> = _authState.asStateFlow()

    private val _isGuestMode = MutableStateFlow<Boolean?>(value = null)
    val isGuestMode: StateFlow<Boolean?> = _isGuestMode.asStateFlow()

    init {
        observeAuthState()
        if (observeGuestModeUseCase != null) {
            observeGuestMode()
        } else {
            _isGuestMode.value = false
        }
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

    private fun observeGuestMode() {
        val useCase = observeGuestModeUseCase ?: return
        viewModelScope.launch {
            useCase().collect { isGuest ->
                _isGuestMode.value = isGuest
            }
        }
    }

    fun sync() {
        viewModelScope.launch {
            syncManager.sync()
        }
    }
}
