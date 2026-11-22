package com.jjcr11.rss.presentation.addsourcescreen

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import co.touchlab.kermit.Logger
import com.jjcr11.rss.domain.usecase.AddSourceUseCase
import com.jjcr11.rss.domain.usecase.GetSourcesUseCase
import com.jjcr11.rss.domain.usecase.ListenToRealtimeSourcesUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class AddSourceViewModel(
    private val addSourceUseCase: AddSourceUseCase,
    private val getSourcesUseCase: GetSourcesUseCase,
    private val listenToRealtimeSourcesUseCase: ListenToRealtimeSourcesUseCase,
) : ViewModel() {

    private val _uiState = MutableStateFlow(value = AddSourceUiState())
    val uiState: StateFlow<AddSourceUiState> = _uiState.asStateFlow()

    init {
        listenToRealtimeSources()
        observeSources()
    }

    private fun listenToRealtimeSources() {
        viewModelScope.launch {
            try {
                listenToRealtimeSourcesUseCase()
            } catch (e: Exception) {
                Logger.withTag(tag = TAG).e(throwable = e) {
                    "Realtime sources failed"
                }
            }
        }
    }

    private fun observeSources() {
        viewModelScope.launch {
            getSourcesUseCase().collect { sources ->
                _uiState.update { currentState ->
                    currentState.copy(
                        sources = sources.map { it.toUiModel() },
                    )
                }
            }
        }
    }

    fun onEvent(event: AddSourceUiEvent) {
        when (event) {
            is AddSourceUiEvent.AddFeed -> addFeed(url = event.url)
            AddSourceUiEvent.ClearError -> clearError()
        }
    }

    private fun addFeed(url: String) {
        val trimmedUrl = url.trim()
        if (trimmedUrl.isBlank()) return

        viewModelScope.launch {
            _uiState.update { currentState ->
                currentState.copy(
                    isLoading = true,
                    errorMessage = null,
                )
            }
            try {
                addSourceUseCase(url = trimmedUrl)
                _uiState.update { currentState ->
                    currentState.copy(
                        isLoading = false,
                        errorMessage = null,
                    )
                }
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

    private companion object {
        const val TAG = "AddSourceViewModel"
    }
}
