package com.jjcr11.rss.presentation.feeddetail

import com.jjcr11.rss.domain.model.Feed
import com.jjcr11.rss.domain.model.Source
import com.jjcr11.rss.domain.repository.FeedRepository
import com.jjcr11.rss.domain.repository.SourceRepository
import com.jjcr11.rss.domain.usecase.GetFeedsUseCase
import com.jjcr11.rss.domain.usecase.GetSourcesUseCase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
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
import kotlin.test.assertFalse
import kotlin.time.Instant
import kotlin.uuid.Uuid

@OptIn(ExperimentalCoroutinesApi::class)
class FeedDetailViewModelTest {

    private val testDispatcher = StandardTestDispatcher()

    private class FakeFeedRepository : FeedRepository {
        val feedsFlow = MutableStateFlow<List<Feed>>(value = emptyList())

        override fun getAll(): Flow<List<Feed>> = feedsFlow

        override suspend fun syncFeeds() {
            throw NotImplementedError()
        }
    }

    private class FakeSourceRepository : SourceRepository {
        val sourcesFlow = MutableStateFlow<List<Source>>(value = emptyList())

        override fun getAll(): Flow<List<Source>> = sourcesFlow

        override suspend fun fetchAndSave(url: String): Source {
            throw NotImplementedError()
        }

        override suspend fun syncSources() {
            throw NotImplementedError()
        }

        override suspend fun listenToRealtimeSources() {
            throw NotImplementedError()
        }
    }

    private lateinit var feedRepository: FakeFeedRepository
    private lateinit var sourceRepository: FakeSourceRepository
    private lateinit var viewModel: FeedDetailViewModel

    @BeforeTest
    fun setUp() {
        Dispatchers.setMain(dispatcher = testDispatcher)
        feedRepository = FakeFeedRepository()
        sourceRepository = FakeSourceRepository()
        viewModel = FeedDetailViewModel(
            getFeedsUseCase = GetFeedsUseCase(repository = feedRepository),
            getSourcesUseCase = GetSourcesUseCase(repository = sourceRepository),
        )
    }

    @AfterTest
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun init_observesFeedsAndMapsSourceName() = runTest {
        val sourceId = Uuid.parse(uuidString = "123e4567-e89b-12d3-a456-426614174000")
        val feedId = Uuid.parse(uuidString = "a1b2c3d4-e5f6-7890-abcd-ef1234567890")
        val now = Instant.parse(input = "2026-11-20T10:00:00Z")

        sourceRepository.sourcesFlow.value = listOf(
            Source(
                id = sourceId,
                name = "TechCrunch",
                rssUrl = "https://techcrunch.com/feed",
                webUrl = "https://techcrunch.com",
                createdAt = now,
                updatedAt = now,
            ),
        )
        feedRepository.feedsFlow.value = listOf(
            Feed(
                id = feedId,
                sourceId = sourceId,
                title = "AI Revolution",
                content = "<p>Article text</p>",
                read = false,
                save = false,
                url = "https://techcrunch.com/ai",
                date = now,
                createdAt = now,
                updatedAt = now,
            ),
        )

        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertFalse(actual = state.isLoading)
        assertEquals(expected = 1, actual = state.feeds.size)
        assertEquals(expected = "TechCrunch", actual = state.feeds.first().sourceName)
        assertEquals(expected = "AI Revolution", actual = state.feeds.first().title)
        assertEquals(expected = "<p>Article text</p>", actual = state.feeds.first().content)
    }

    @Test
    fun setInitialFeedId_calculatesCorrectInitialIndex() = runTest {
        val sourceId = Uuid.parse(uuidString = "123e4567-e89b-12d3-a456-426614174000")
        val feedId1 = Uuid.parse(uuidString = "a1b2c3d4-e5f6-7890-abcd-ef1234567890")
        val feedId2 = Uuid.parse(uuidString = "b2c3d4e5-f6a7-8901-bcde-f12345678901")
        val now = Instant.parse(input = "2026-11-20T10:00:00Z")

        feedRepository.feedsFlow.value = listOf(
            Feed(
                id = feedId1,
                sourceId = sourceId,
                title = "Article 1",
                content = "",
                read = false,
                save = false,
                url = "https://example.com/1",
                date = now,
                createdAt = now,
                updatedAt = now,
            ),
            Feed(
                id = feedId2,
                sourceId = sourceId,
                title = "Article 2",
                content = "",
                read = false,
                save = false,
                url = "https://example.com/2",
                date = now,
                createdAt = now,
                updatedAt = now,
            ),
        )

        viewModel.setInitialFeedId(feedId = feedId2.toString())
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertEquals(expected = 1, actual = state.initialIndex)
    }

    @Test
    fun setInitialFeedId_beforeFeedsEmitted_updatesIndexWhenFeedsArrive() = runTest {
        val sourceId = Uuid.parse(uuidString = "123e4567-e89b-12d3-a456-426614174000")
        val feedId1 = Uuid.parse(uuidString = "a1b2c3d4-e5f6-7890-abcd-ef1234567890")
        val feedId2 = Uuid.parse(uuidString = "b2c3d4e5-f6a7-8901-bcde-f12345678901")
        val now = Instant.parse(input = "2026-11-20T10:00:00Z")

        viewModel.setInitialFeedId(feedId = feedId2.toString())

        feedRepository.feedsFlow.value = listOf(
            Feed(
                id = feedId1,
                sourceId = sourceId,
                title = "Article 1",
                content = "",
                read = false,
                save = false,
                url = "https://example.com/1",
                date = now,
                createdAt = now,
                updatedAt = now,
            ),
            Feed(
                id = feedId2,
                sourceId = sourceId,
                title = "Article 2",
                content = "",
                read = false,
                save = false,
                url = "https://example.com/2",
                date = now,
                createdAt = now,
                updatedAt = now,
            ),
        )

        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertEquals(expected = 1, actual = state.initialIndex)
    }
}
