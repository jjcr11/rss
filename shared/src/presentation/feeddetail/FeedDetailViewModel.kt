package com.jjcr11.rss.presentation.feeddetail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.jjcr11.rss.domain.usecase.GetFeedsUseCase
import com.jjcr11.rss.domain.usecase.GetSourcesUseCase
import com.jjcr11.rss.domain.usecase.SetFeedAsReadUseCase
import com.jjcr11.rss.presentation.feedsscreen.toUiModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlin.uuid.Uuid

class FeedDetailViewModel(
    private val getFeedsUseCase: GetFeedsUseCase,
    private val getSourcesUseCase: GetSourcesUseCase,
    private val setFeedAsReadUseCase: SetFeedAsReadUseCase,
) : ViewModel() {

    private val _uiState = MutableStateFlow(value = FeedDetailUiState())
    val uiState: StateFlow<FeedDetailUiState> = _uiState.asStateFlow()

    private var targetFeedId: String? = null

    fun setInitialFeedId(feedId: String) {
        if (targetFeedId == feedId) return
        targetFeedId = feedId
        updateInitialIndex()
    }

    init {
        observeFeeds()
    }

    private fun observeFeeds() {
        viewModelScope.launch {
            val feedUiModels = combine(
                flow = getFeedsUseCase(),
                flow2 = getSourcesUseCase(),
                transform = { feeds, sources ->
                    val sourcesMap = sources.associateBy { source -> source.id }
                    feeds.map { feed ->
                        feed.toUiModel(
                            sourceName = sourcesMap[feed.sourceId]?.name.orEmpty(),
                        )
                    }
                },
            ).first()

            _uiState.update { currentState ->
                val calculatedIndex = targetFeedId?.let { id ->
                    feedUiModels.indexOfFirst { it.id == id }.coerceAtLeast(minimumValue = 0)
                } ?: currentState.initialIndex

                currentState.copy(
                    feeds = feedUiModels,
                    initialIndex = calculatedIndex,
                    isLoading = false,
                )
            }
        }
    }

    private fun updateInitialIndex() {
        val currentFeeds = _uiState.value.feeds
        val id = targetFeedId
        if ((id != null) && currentFeeds.isNotEmpty()) {
            val index = currentFeeds.indexOfFirst { it.id == id }.coerceAtLeast(minimumValue = 0)
            _uiState.update { currentState ->
                currentState.copy(initialIndex = index)
            }
        }
    }

    fun onPageChanged(index: Int) {
        val feeds = _uiState.value.feeds
        if (index in feeds.indices) {
            val feed = feeds[index]
            markAsRead(feedId = feed.id)
        }
    }

    private fun markAsRead(feedId: String) {
        viewModelScope.launch {
            try {
                val uuid = Uuid.parse(uuidString = feedId)
                setFeedAsReadUseCase(id = uuid)
            } catch (_: Exception) {
                // Silently ignore parsing errors
            }
        }
    }
}
