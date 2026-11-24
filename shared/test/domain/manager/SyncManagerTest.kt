package com.jjcr11.rss.domain.manager

import com.jjcr11.rss.domain.model.Feed
import com.jjcr11.rss.domain.model.Source
import com.jjcr11.rss.domain.repository.FeedRepository
import com.jjcr11.rss.domain.repository.SourceRepository
import com.jjcr11.rss.domain.usecase.FetchFeedsFromSourcesUseCase
import com.jjcr11.rss.domain.usecase.SyncFeedsUseCase
import com.jjcr11.rss.domain.usecase.SyncSourcesUseCase
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.uuid.Uuid

class SyncManagerTest {

    private class FakeSourceRepository : SourceRepository {
        var syncSourcesCalled = false
        var syncShouldThrow: Exception? = null

        override suspend fun fetchAndSave(url: String): Source = throw NotImplementedError()

        override suspend fun syncSources() {
            syncSourcesCalled = true
            syncShouldThrow?.let { throw it }
        }

        override suspend fun listenToRealtimeSources() = Unit

        override fun getAll(): Flow<List<Source>> = flowOf(value = emptyList())

        override suspend fun delete(source: Source): Boolean = true
    }

    private class FakeFeedRepository : FeedRepository {
        var syncFeedsCalled = false
        var syncFeedsShouldThrow: Exception? = null
        var fetchFeedsFromSourcesCalled = false

        override fun getAll(): Flow<List<Feed>> = flowOf(value = emptyList())

        override suspend fun syncFeeds() {
            syncFeedsCalled = true
            syncFeedsShouldThrow?.let { throw it }
        }

        override suspend fun setAsRead(id: Uuid) = Unit

        override suspend fun fetchFeedsFromSources() {
            fetchFeedsFromSourcesCalled = true
        }
    }

    @Test
    fun sync_callsBothSyncSourcesAndSyncFeeds() = runTest {
        val sourceRepository = FakeSourceRepository()
        val feedRepository = FakeFeedRepository()
        val syncManager = SyncManager(
            syncSourcesUseCase = SyncSourcesUseCase(repository = sourceRepository),
            syncFeedsUseCase = SyncFeedsUseCase(repository = feedRepository),
        )

        syncManager.sync()

        assertEquals(
            expected = true,
            actual = sourceRepository.syncSourcesCalled,
        )
        assertEquals(
            expected = true,
            actual = feedRepository.syncFeedsCalled,
        )
        assertFalse(actual = syncManager.isSyncing.value)
    }

    @Test
    fun sync_whenFetchFeedsFromSourcesProvided_callsIt() = runTest {
        val sourceRepository = FakeSourceRepository()
        val feedRepository = FakeFeedRepository()
        val syncManager = SyncManager(
            syncSourcesUseCase = SyncSourcesUseCase(repository = sourceRepository),
            syncFeedsUseCase = SyncFeedsUseCase(repository = feedRepository),
            fetchFeedsFromSourcesUseCase = FetchFeedsFromSourcesUseCase(repository = feedRepository),
        )

        syncManager.sync()

        assertEquals(
            expected = true,
            actual = sourceRepository.syncSourcesCalled,
        )
        assertEquals(
            expected = true,
            actual = feedRepository.syncFeedsCalled,
        )
        assertEquals(
            expected = true,
            actual = feedRepository.fetchFeedsFromSourcesCalled,
        )
        assertFalse(actual = syncManager.isSyncing.value)
    }

    @Test
    fun sync_whenSyncFails_catchesExceptionAndResetsSyncingState() = runTest {
        val sourceRepository = FakeSourceRepository().apply {
            syncShouldThrow = RuntimeException("Network error")
        }
        val feedRepository = FakeFeedRepository()
        val syncManager = SyncManager(
            syncSourcesUseCase = SyncSourcesUseCase(repository = sourceRepository),
            syncFeedsUseCase = SyncFeedsUseCase(repository = feedRepository),
        )

        syncManager.sync()

        assertFalse(actual = syncManager.isSyncing.value)
    }
}
