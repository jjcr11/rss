package com.jjcr11.rss.domain.usecase

import com.jjcr11.rss.domain.model.Source
import com.jjcr11.rss.domain.repository.SourceRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals

class SyncSourcesUseCaseTest {

    private class FakeSourceRepository : SourceRepository {
        var syncSourcesCalled = false

        override suspend fun fetchAndSave(url: String): Source {
            throw NotImplementedError()
        }

        override suspend fun syncSources() {
            syncSourcesCalled = true
        }

        override suspend fun listenToRealtimeSources() {
            throw NotImplementedError()
        }

        override fun getAll(): Flow<List<Source>> = flowOf(emptyList())

        override suspend fun delete(source: Source): Boolean = true
    }

    @Test
    fun invoke_callsSyncSourcesOnRepository() = runTest {
        val repository = FakeSourceRepository()
        val useCase = SyncSourcesUseCase(repository = repository)

        useCase()

        assertEquals(
            expected = true,
            actual = repository.syncSourcesCalled,
        )
    }
}
