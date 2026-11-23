package com.jjcr11.rss.presentation.feedsscreen

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
import kotlin.test.assertTrue
import kotlin.time.Instant
import kotlin.uuid.Uuid

@OptIn(ExperimentalCoroutinesApi::class)
class FeedsViewModelTest {

    private val testDispatcher = StandardTestDispatcher()

    private class FakeFeedRepository : FeedRepository {
        val feedsFlow = MutableStateFlow<List<Feed>>(value = emptyList())

        override fun getAll(): Flow<List<Feed>> = feedsFlow

        override suspend fun syncFeeds() = Unit

        override suspend fun setAsRead(id: Uuid) = Unit
    }

    private class FakeSourceRepository : SourceRepository {
        val sourcesFlow = MutableStateFlow<List<Source>>(value = emptyList())

        override suspend fun fetchAndSave(url: String): Source = throw NotImplementedError()

        override suspend fun syncSources() = Unit

        override suspend fun listenToRealtimeSources() = Unit

        override fun getAll(): Flow<List<Source>> = sourcesFlow

        override suspend fun delete(source: Source): Boolean = true
    }

    private lateinit var feedRepository: FakeFeedRepository
    private lateinit var sourceRepository: FakeSourceRepository
    private lateinit var getFeedsUseCase: GetFeedsUseCase
    private lateinit var getSourcesUseCase: GetSourcesUseCase
    private lateinit var viewModel: FeedsViewModel

    @BeforeTest
    fun setUp() {
        Dispatchers.setMain(dispatcher = testDispatcher)
        feedRepository = FakeFeedRepository()
        sourceRepository = FakeSourceRepository()
        getFeedsUseCase = GetFeedsUseCase(repository = feedRepository)
        getSourcesUseCase = GetSourcesUseCase(repository = sourceRepository)
        viewModel = FeedsViewModel(
            getFeedsUseCase = getFeedsUseCase,
            getSourcesUseCase = getSourcesUseCase,
        )
    }

    @AfterTest
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun initialState_hasEmptyFeedsAndLoadingTrue() {
        val (feeds, isLoading) = viewModel.uiState.value

        assertTrue(actual = feeds.isEmpty())
        assertTrue(actual = isLoading)
    }

    @Test
    fun init_observesFeedsAndMapsSourceNameCorrectly() = runTest(context = testDispatcher) {
        val sourceId = Uuid.random()
        val instant = Instant.parse(input = "2024-11-04T10:00:00Z")
        val source = Source(
            id = sourceId,
            rssUrl = "https://theverge.com/rss",
            webUrl = "https://theverge.com",
            name = "The Verge",
            createdAt = instant,
            updatedAt = instant,
        )
        val feed = Feed(
            id = Uuid.random(),
            sourceId = sourceId,
            title = "Apple Silicon M4 Max Benchmarks",
            isRead = false,
            isSaved = false,
            url = "https://theverge.com/m4",
            date = instant,
            createdAt = instant,
            updatedAt = instant,
        )

        sourceRepository.sourcesFlow.value = listOf(source)
        feedRepository.feedsFlow.value = listOf(feed)
        advanceUntilIdle()

        val (feeds, isLoading) = viewModel.uiState.value
        assertFalse(actual = isLoading)
        assertEquals(
            expected = 1,
            actual = feeds.size,
        )
        assertEquals(
            expected = "The Verge",
            actual = feeds[0].sourceName,
        )
        assertEquals(
            expected = "Apple Silicon M4 Max Benchmarks",
            actual = feeds[0].title,
        )
        assertEquals(
            expected = "Nov 4, 2024",
            actual = feeds[0].date,
        )
    }

    @Test
    fun init_whenNoMatchingSource_usesEmptySourceName() = runTest(context = testDispatcher) {
        val instant = Instant.parse(input = "2024-11-04T10:00:00Z")
        val feed = Feed(
            id = Uuid.random(),
            sourceId = Uuid.random(),
            title = "Unknown Feed",
            isRead = false,
            isSaved = false,
            url = "https://example.com",
            date = instant,
            createdAt = instant,
            updatedAt = instant,
        )

        feedRepository.feedsFlow.value = listOf(feed)
        sourceRepository.sourcesFlow.value = emptyList()
        advanceUntilIdle()

        val (feeds, isLoading) = viewModel.uiState.value
        assertFalse(actual = isLoading)
        assertEquals(
            expected = 1,
            actual = feeds.size,
        )
        assertEquals(
            expected = "",
            actual = feeds[0].sourceName,
        )
    }

    @Test
    fun init_hidesFeedsThatWereAlreadyReadOnInitialEmission() = runTest(context = testDispatcher) {
        val instant = Instant.parse(input = "2024-11-04T10:00:00Z")
        val unreadFeed = Feed(
            id = Uuid.random(),
            sourceId = Uuid.random(),
            title = "Unread Feed",
            isRead = false,
            isSaved = false,
            url = "https://example.com/unread",
            date = instant,
            createdAt = instant,
            updatedAt = instant,
        )
        val alreadyReadFeed = Feed(
            id = Uuid.random(),
            sourceId = Uuid.random(),
            title = "Already Read Feed",
            isRead = true,
            isSaved = false,
            url = "https://example.com/read",
            date = instant,
            createdAt = instant,
            updatedAt = instant,
        )

        feedRepository.feedsFlow.value = listOf(unreadFeed, alreadyReadFeed)
        advanceUntilIdle()

        val (feeds, isLoading) = viewModel.uiState.value
        assertFalse(actual = isLoading)
        assertEquals(
            expected = 1,
            actual = feeds.size,
        )
        assertEquals(
            expected = unreadFeed.id.toString(),
            actual = feeds[0].id,
        )
    }

    @Test
    fun feedMarkedAsReadDuringSession_remainsInListWithIsReadTrue() = runTest(context = testDispatcher) {
        val instant = Instant.parse(input = "2024-11-04T10:00:00Z")
        val feedId = Uuid.random()
        val initialFeed = Feed(
            id = feedId,
            sourceId = Uuid.random(),
            title = "Session Feed",
            isRead = false,
            isSaved = false,
            url = "https://example.com/session",
            date = instant,
            createdAt = instant,
            updatedAt = instant,
        )

        feedRepository.feedsFlow.value = listOf(initialFeed)
        advanceUntilIdle()

        assertEquals(
            expected = 1,
            actual = viewModel.uiState.value.feeds.size,
        )
        assertFalse(actual = viewModel.uiState.value.feeds[0].isRead)

        val updatedFeed = initialFeed.copy(isRead = true)
        feedRepository.feedsFlow.value = listOf(updatedFeed)
        advanceUntilIdle()

        val (feeds, _) = viewModel.uiState.value
        assertEquals(
            expected = 1,
            actual = feeds.size,
        )
        assertTrue(actual = feeds[0].isRead)
    }
}
