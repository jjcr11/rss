package com.jjcr11.rss.domain.usecase

import com.jjcr11.rss.domain.model.Feed
import com.jjcr11.rss.domain.repository.FeedRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.uuid.Uuid

class SetFeedAsReadUseCaseTest {

    private class FakeFeedRepository : FeedRepository {
        var setAsReadId: Uuid? = null

        override fun getAll(): Flow<List<Feed>> = flowOf(value = emptyList())

        override suspend fun syncFeeds() = Unit

        override suspend fun setAsRead(id: Uuid) {
            setAsReadId = id
        }
    }

    @Test
    fun invoke_callsSetAsReadOnRepository() = runTest {
        val repository = FakeFeedRepository()
        val useCase = SetFeedAsReadUseCase(repository = repository)
        val feedId = Uuid.random()

        useCase(id = feedId)

        assertEquals(
            expected = feedId,
            actual = repository.setAsReadId,
        )
    }
}
