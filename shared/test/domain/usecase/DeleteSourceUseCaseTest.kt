package com.jjcr11.rss.domain.usecase

import com.jjcr11.rss.domain.model.Source
import com.jjcr11.rss.domain.repository.SourceRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertSame
import kotlin.time.Clock
import kotlin.uuid.Uuid

class DeleteSourceUseCaseTest {

    private class FakeSourceRepository : SourceRepository {
        var lastDeletedSource: Source? = null
        var deleteResultToReturn: Boolean = true

        override suspend fun fetchAndSave(url: String): Source {
            throw NotImplementedError()
        }

        override suspend fun syncSources() {
            throw NotImplementedError()
        }

        override suspend fun listenToRealtimeSources() {
            throw NotImplementedError()
        }

        override fun getAll(): Flow<List<Source>> = flowOf(emptyList())

        override suspend fun delete(source: Source): Boolean {
            lastDeletedSource = source
            return deleteResultToReturn
        }
    }

    @Test
    fun invoke_callsDeleteOnRepositoryAndReturnsResult() = runTest {
        val repository = FakeSourceRepository()
        val useCase = DeleteSourceUseCase(repository = repository)
        val sourceToDelete = Source(
            id = Uuid.random(),
            rssUrl = "https://example.com/rss",
            webUrl = "https://example.com",
            name = "Example Source",
            createdAt = Clock.System.now(),
            updatedAt = Clock.System.now(),
        )

        val result = useCase(source = sourceToDelete)

        assertEquals(
            expected = true,
            actual = result,
        )
        assertSame(
            expected = sourceToDelete,
            actual = repository.lastDeletedSource,
        )
    }
}
