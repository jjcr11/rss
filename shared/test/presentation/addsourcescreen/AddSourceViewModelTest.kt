package com.jjcr11.rss.presentation.addsourcescreen

import com.jjcr11.rss.domain.model.Source
import com.jjcr11.rss.domain.repository.SourceRepository
import com.jjcr11.rss.domain.usecase.AddSourceUseCase
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
import kotlin.test.assertNull
import kotlin.test.assertTrue
import kotlin.time.Clock
import kotlin.uuid.Uuid

@OptIn(ExperimentalCoroutinesApi::class)
class AddSourceViewModelTest {

    private val testDispatcher = StandardTestDispatcher()

    private class FakeSourceRepository : SourceRepository {
        var lastFetchedUrl: String? = null
        var sourceToReturn: Source? = null
        var shouldThrow: Exception? = null
        val savedSources = mutableListOf<Source>()
        val sourcesFlow = MutableStateFlow<List<Source>>(value = emptyList())

        override suspend fun fetchAndSave(url: String): Source {
            lastFetchedUrl = url
            shouldThrow?.let { throw it }
            val source = sourceToReturn ?: throw IllegalStateException("sourceToReturn not set")
            savedSources.add(element = source)
            sourcesFlow.value = savedSources.toList()
            return source
        }

        override fun getAll(): Flow<List<Source>> = sourcesFlow
    }

    private lateinit var repository: FakeSourceRepository
    private lateinit var addSourceUseCase: AddSourceUseCase
    private lateinit var getSourcesUseCase: GetSourcesUseCase
    private lateinit var viewModel: AddSourceViewModel

    @BeforeTest
    fun setUp() {
        Dispatchers.setMain(dispatcher = testDispatcher)
        repository = FakeSourceRepository()
        addSourceUseCase = AddSourceUseCase(repository = repository)
        getSourcesUseCase = GetSourcesUseCase(repository = repository)
        viewModel = AddSourceViewModel(
            addSourceUseCase = addSourceUseCase,
            getSourcesUseCase = getSourcesUseCase,
        )
    }

    @AfterTest
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun initialState_hasEmptySourcesNotLoadingAndNoError() {
        val (sources, isLoading, errorMessage) = viewModel.uiState.value

        assertTrue(actual = sources.isEmpty())
        assertFalse(actual = isLoading)
        assertNull(actual = errorMessage)
    }

    @Test
    fun init_observesSavedSourcesFromGetSourcesUseCase() = runTest(context = testDispatcher) {
        val existingSource = Source(
            id = Uuid.random(),
            rssUrl = "https://example.com/existing.xml",
            webUrl = "https://example.com",
            name = "Existing Source",
            createdAt = Clock.System.now(),
            updatedAt = Clock.System.now(),
        )
        repository.sourcesFlow.value = listOf(existingSource)
        advanceUntilIdle()

        val (sources) = viewModel.uiState.value
        assertEquals(
            expected = 1,
            actual = sources.size,
        )
        assertEquals(
            expected = "Existing Source",
            actual = sources[0].name,
        )
    }

    @Test
    fun onEvent_addFeed_whenSuccessful_updatesSourcesAndStopsLoading() = runTest(context = testDispatcher) {
        val expectedSource = Source(
            id = Uuid.random(),
            rssUrl = "https://example.com/rss",
            webUrl = "https://example.com",
            name = "Example RSS",
            createdAt = Clock.System.now(),
            updatedAt = Clock.System.now(),
        )
        repository.sourceToReturn = expectedSource

        viewModel.onEvent(event = AddSourceUiEvent.AddFeed(url = "https://example.com/rss"))
        advanceUntilIdle()

        val (sources, isLoading, errorMessage) = viewModel.uiState.value
        assertEquals(
            expected = 1,
            actual = sources.size,
        )
        assertEquals(
            expected = expectedSource.id.toString(),
            actual = sources[0].id,
        )
        assertEquals(
            expected = "Example RSS",
            actual = sources[0].name,
        )
        assertEquals(
            expected = "https://example.com",
            actual = sources[0].url,
        )
        assertFalse(actual = isLoading)
        assertNull(actual = errorMessage)
    }

    @Test
    fun onEvent_addFeed_whenFails_setsErrorMessageAndStopsLoading() = runTest(context = testDispatcher) {
        repository.shouldThrow = IllegalStateException("Failed to parse RSS feed")

        viewModel.onEvent(event = AddSourceUiEvent.AddFeed(url = "https://example.com/rss"))
        advanceUntilIdle()

        val (sources, isLoading, errorMessage) = viewModel.uiState.value
        assertTrue(actual = sources.isEmpty())
        assertFalse(actual = isLoading)
        assertEquals(
            expected = "Failed to parse RSS feed",
            actual = errorMessage,
        )
    }

    @Test
    fun onEvent_addFeed_whenUrlIsBlank_doesNotCallUseCase() = runTest(context = testDispatcher) {
        viewModel.onEvent(event = AddSourceUiEvent.AddFeed(url = "   "))
        advanceUntilIdle()

        assertNull(actual = repository.lastFetchedUrl)
        val (sources, isLoading, errorMessage) = viewModel.uiState.value
        assertTrue(actual = sources.isEmpty())
        assertFalse(actual = isLoading)
        assertNull(actual = errorMessage)
    }

    @Test
    fun onEvent_clearError_resetsErrorMessageToNull() = runTest(context = testDispatcher) {
        repository.shouldThrow = IllegalStateException("Something went wrong")
        viewModel.onEvent(event = AddSourceUiEvent.AddFeed(url = "https://example.com/rss"))
        advanceUntilIdle()

        assertEquals(
            expected = "Something went wrong",
            actual = viewModel.uiState.value.errorMessage,
        )

        viewModel.onEvent(event = AddSourceUiEvent.ClearError)

        assertNull(actual = viewModel.uiState.value.errorMessage)
    }
}
