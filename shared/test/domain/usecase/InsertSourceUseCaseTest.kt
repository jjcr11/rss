package com.jjcr11.rss.domain.usecase

import com.jjcr11.rss.domain.model.Source
import com.jjcr11.rss.domain.repository.SourceRepository
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.time.Clock
import kotlin.uuid.Uuid

class InsertSourceUseCaseTest {

    private class FakeSourceRepository : SourceRepository {
        val insertedSources = mutableListOf<Source>()

        override suspend fun insert(source: Source) {
            insertedSources.add(element = source)
        }
    }

    @Test
    fun invoke_insertsSourceIntoRepository() = runTest {
        val repository = FakeSourceRepository()
        val useCase = InsertSourceUseCase(repository = repository)
        val source = Source(
            id = Uuid.random(),
            rssUrl = "https://example.com/rss",
            webUrl = "https://example.com",
            name = "Example RSS",
            createdAt = Clock.System.now(),
            updatedAt = Clock.System.now(),
        )

        useCase(source = source)

        assertEquals(
            expected = 1,
            actual = repository.insertedSources.size,
        )
        assertEquals(
            expected = source,
            actual = repository.insertedSources.first(),
        )
    }
}
