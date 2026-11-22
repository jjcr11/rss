package com.jjcr11.rss.domain.usecase

import com.jjcr11.rss.domain.model.Feed
import com.jjcr11.rss.domain.repository.FeedRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals

class SyncFeedsUseCaseTest {

    private class FakeFeedRepository : FeedRepository {
        var syncFeedsCalled = false

        override fun getAll(): Flow<List<Feed>> = flowOf(value = emptyList())

        override suspend fun syncFeeds() {
            syncFeedsCalled = true
        }
    }

    @Test
    fun invoke_callsSyncFeedsOnRepository() = runTest {
        val repository = FakeFeedRepository()
        val useCase = SyncFeedsUseCase(repository = repository)

        useCase()

        assertEquals(
            expected = true,
            actual = repository.syncFeedsCalled,
        )
    }
}
