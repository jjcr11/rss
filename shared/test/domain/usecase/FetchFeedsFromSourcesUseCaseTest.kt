package com.jjcr11.rss.domain.usecase

import com.jjcr11.rss.domain.model.Feed
import com.jjcr11.rss.domain.repository.FeedRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.uuid.Uuid

class FetchFeedsFromSourcesUseCaseTest {

    private class FakeFeedRepository : FeedRepository {
        var fetchFeedsFromSourcesCalled = false

        override fun getAll(): Flow<List<Feed>> = flowOf(value = emptyList())

        override suspend fun syncFeeds() = Unit

        override suspend fun setAsRead(id: Uuid) = Unit

        override suspend fun fetchFeedsFromSources() {
            fetchFeedsFromSourcesCalled = true
        }
    }

    @Test
    fun invoke_callsFetchFeedsFromSourcesOnRepository() = runTest {
        val repository = FakeFeedRepository()
        val useCase = FetchFeedsFromSourcesUseCase(repository = repository)

        useCase()

        assertEquals(
            expected = true,
            actual = repository.fetchFeedsFromSourcesCalled,
        )
    }
}
