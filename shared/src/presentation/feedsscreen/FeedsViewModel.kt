package com.jjcr11.rss.presentation.feedsscreen

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.jjcr11.rss.domain.usecase.GetFeedsUseCase
import com.jjcr11.rss.domain.usecase.GetSourcesUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class FeedsViewModel(
    private val getFeedsUseCase: GetFeedsUseCase,
    private val getSourcesUseCase: GetSourcesUseCase,
) : ViewModel() {

    private val _uiState = MutableStateFlow(value = FeedsUiState(isLoading = true))
    val uiState: StateFlow<FeedsUiState> = _uiState.asStateFlow()

    private val initiallyHiddenFeedIds = mutableSetOf<String>()
    private var isInitialEmission = true

    init {
        observeFeeds()
    }

    private fun observeFeeds() {
        viewModelScope.launch {
            combine(
                flow = getFeedsUseCase(),
                flow2 = getSourcesUseCase(),
                transform = { feeds, sources ->
                    if (isInitialEmission && feeds.isNotEmpty()) {
                        initiallyHiddenFeedIds.addAll(
                            elements = feeds.filter { feed -> feed.isRead }
                                .map { feed -> feed.id.toString() },
                        )
                        isInitialEmission = false
                    }

                    val visibleFeeds = feeds.filterNot { feed ->
                        feed.id.toString() in initiallyHiddenFeedIds
                    }

                    val sourcesMap = sources.associateBy { source -> source.id }
                    visibleFeeds.map { feed ->
                        feed.toUiModel(
                            sourceName = sourcesMap[feed.sourceId]?.name.orEmpty(),
                        )
                    }
                },
            ).collect { feedUiModels ->
                _uiState.update { currentState ->
                    currentState.copy(
                        feeds = feedUiModels,
                        isLoading = false,
                    )
                }
            }
        }
    }
}
