package com.jjcr11.rss.domain.usecase

import com.jjcr11.rss.domain.model.Source
import com.jjcr11.rss.domain.repository.SourceRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals

class ListenToRealtimeSourcesUseCaseTest {

    private class FakeSourceRepository : SourceRepository {
        var listenToRealtimeSourcesCalled = false

        override suspend fun fetchAndSave(url: String): Source {
            throw NotImplementedError()
        }

        override suspend fun syncSources() {
            throw NotImplementedError()
        }

        override suspend fun listenToRealtimeSources() {
            listenToRealtimeSourcesCalled = true
        }

        override fun getAll(): Flow<List<Source>> = flowOf(emptyList())

        override suspend fun delete(source: Source): Boolean = true
    }

    @Test
    fun invoke_callsListenToRealtimeSourcesOnRepository() = runTest {
        val repository = FakeSourceRepository()
        val useCase = ListenToRealtimeSourcesUseCase(repository = repository)

        useCase()

        assertEquals(
            expected = true,
            actual = repository.listenToRealtimeSourcesCalled,
        )
    }
}
