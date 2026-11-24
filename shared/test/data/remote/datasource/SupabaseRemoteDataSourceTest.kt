package com.jjcr11.rss.data.remote.datasource

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import com.jjcr11.rss.data.local.preferences.UserPreferencesDataSource
import com.jjcr11.rss.data.local.preferences.createDataStore
import com.jjcr11.rss.data.remote.dto.FeedDto
import com.jjcr11.rss.data.remote.dto.SourceDto
import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.postgrest.Postgrest
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import kotlin.uuid.Uuid

class SupabaseRemoteDataSourceTest {

    private fun createTestDataStore(): DataStore<Preferences> {
        return createDataStore(
            producePath = { "/tmp/test_pref_${Uuid.random()}.preferences_pb" },
        )
    }

    private val fakeSupabaseClient = createSupabaseClient(
        supabaseUrl = "https://example.supabase.co",
        supabaseKey = "anon",
    ) {
        install(plugin = Postgrest)
    }

    @Test
    fun whenGuestModeIsTrue_allOperationsReturnSafelyWithoutCallingRemote() = runTest {
        val dataStore = createTestDataStore()
        val userPreferencesDataSource = UserPreferencesDataSource(dataStore = dataStore)
        userPreferencesDataSource.setGuestMode(isGuestMode = true)

        val dataSource = SupabaseRemoteDataSource(
            supabaseClient = fakeSupabaseClient,
            userPreferencesDataSource = userPreferencesDataSource,
        )

        val sources = dataSource.getSources()
        assertTrue(actual = sources.isEmpty())

        val unreadFeeds = dataSource.getUnreadFeeds()
        assertTrue(actual = unreadFeeds.isEmpty())

        val sampleFeed = FeedDto(
            id = "feed-1",
            sourceId = "source-1",
            title = "Title",
            isRead = false,
            isSaved = false,
            url = "https://example.com/feed-1",
            date = 1000L,
            createdAt = 1000L,
            updatedAt = 1000L,
        )
        val sampleSource = SourceDto(
            id = "source-1",
            rssUrl = "https://example.com/rss",
            webUrl = "https://example.com",
            name = "Example Source",
            createdAt = 1000L,
            updatedAt = 1000L,
        )

        dataSource.insertFeeds(feeds = listOf(sampleFeed))
        dataSource.insertSourceWithFeeds(
            source = sampleSource,
            feeds = listOf(sampleFeed),
        )
        dataSource.setFeedAsRead(id = "feed-1")
        dataSource.setFeedsAsRead(ids = listOf("feed-1"))
        dataSource.deleteSource(id = "source-1")
        dataSource.deleteSources(ids = listOf("source-1"))
        dataSource.deleteFeeds(ids = listOf("feed-1"))

        val events = dataSource.observeSources().toList()
        assertEquals(
            expected = 0,
            actual = events.size,
        )
    }
}
