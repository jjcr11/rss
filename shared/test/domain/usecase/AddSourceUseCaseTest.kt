package com.jjcr11.rss.domain.usecase

import com.jjcr11.rss.domain.model.Source
import com.jjcr11.rss.domain.repository.SourceRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.time.Clock
import kotlin.uuid.Uuid

class AddSourceUseCaseTest {

    private class FakeSourceRepository : SourceRepository {
        var lastFetchedUrl: String? = null
        var sourceToReturn: Source? = null

        override suspend fun fetchAndSave(url: String): Source {
            lastFetchedUrl = url
            return sourceToReturn ?: throw IllegalStateException("sourceToReturn not set")
        }

        override fun getAll(): Flow<List<Source>> = flowOf(emptyList())
    }

    @Test
    fun invoke_callsFetchAndSaveOnRepositoryAndReturnsSource() = runTest {
        val repository = FakeSourceRepository()
        val useCase = AddSourceUseCase(repository = repository)
        val expectedSource = Source(
            id = Uuid.random(),
            rssUrl = "https://example.com/rss",
            webUrl = "https://example.com",
            name = "Example RSS",
            createdAt = Clock.System.now(),
            updatedAt = Clock.System.now(),
        )
        repository.sourceToReturn = expectedSource

        val result = useCase(url = "https://example.com/rss")

        assertEquals(
            expected = "https://example.com/rss",
            actual = repository.lastFetchedUrl,
        )
        assertEquals(
            expected = expectedSource,
            actual = result,
        )
    }
}
