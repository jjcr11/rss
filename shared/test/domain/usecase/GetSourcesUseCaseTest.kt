package com.jjcr11.rss.domain.usecase

import com.jjcr11.rss.domain.model.Source
import com.jjcr11.rss.domain.repository.SourceRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.time.Clock
import kotlin.uuid.Uuid

class GetSourcesUseCaseTest {

    private class FakeSourceRepository : SourceRepository {
        var sourcesFlow: Flow<List<Source>> = flowOf(emptyList())

        override suspend fun fetchAndSave(url: String): Source {
            throw NotImplementedError()
        }

        override fun getAll(): Flow<List<Source>> = sourcesFlow
    }

    @Test
    fun invoke_returnsFlowOfSourcesFromRepository() = runTest {
        val repository = FakeSourceRepository()
        val useCase = GetSourcesUseCase(repository = repository)
        val expectedSource = Source(
            id = Uuid.random(),
            rssUrl = "https://example.com/rss",
            webUrl = "https://example.com",
            name = "Example RSS",
            createdAt = Clock.System.now(),
            updatedAt = Clock.System.now(),
        )
        repository.sourcesFlow = flowOf(listOf(expectedSource))

        val result = useCase().first()

        assertEquals(
            expected = 1,
            actual = result.size,
        )
        assertEquals(
            expected = expectedSource,
            actual = result.first(),
        )
    }
}
