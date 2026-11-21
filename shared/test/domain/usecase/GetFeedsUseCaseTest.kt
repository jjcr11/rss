package com.jjcr11.rss.domain.usecase

import com.jjcr11.rss.domain.model.Feed
import com.jjcr11.rss.domain.repository.FeedRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.time.Clock
import kotlin.uuid.Uuid

class GetFeedsUseCaseTest {

    private class FakeFeedRepository : FeedRepository {
        var feedsFlow: Flow<List<Feed>> = flowOf(emptyList())

        override fun getAll(): Flow<List<Feed>> = feedsFlow
    }

    @Test
    fun invoke_returnsFlowOfFeedsFromRepository() = runTest {
        val repository = FakeFeedRepository()
        val useCase = GetFeedsUseCase(repository = repository)
        val now = Clock.System.now()
        val expectedFeed = Feed(
            id = Uuid.random(),
            sourceId = Uuid.random(),
            title = "Sample News",
            read = false,
            save = false,
            url = "https://example.com/news",
            date = now,
            createdAt = now,
            updatedAt = now,
        )
        repository.feedsFlow = flowOf(listOf(expectedFeed))

        val result = useCase().first()

        assertEquals(
            expected = 1,
            actual = result.size,
        )
        assertEquals(
            expected = expectedFeed,
            actual = result.first(),
        )
    }
}
