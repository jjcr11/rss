package com.jjcr11.rss.data.local.db

import androidx.room.Room
import androidx.sqlite.driver.bundled.BundledSQLiteDriver
import com.jjcr11.rss.data.local.dao.FeedDao
import com.jjcr11.rss.data.local.dao.SourceDao
import com.jjcr11.rss.data.local.entity.FeedEntity
import com.jjcr11.rss.data.local.entity.SourceEntity
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals

class RssDatabaseTest {

    private lateinit var database: RssDatabase
    private lateinit var sourceDao: SourceDao
    private lateinit var feedDao: FeedDao

    @BeforeTest
    fun setUp() {
        database = Room.inMemoryDatabaseBuilder<RssDatabase>()
            .setDriver(driver = BundledSQLiteDriver())
            .build()
        sourceDao = database.sourceDao()
        feedDao = database.feedDao()
    }

    @AfterTest
    fun tearDown() {
        database.close()
    }

    @Test
    fun insertSource_savesAndRetrievesFromDatabase() = runTest {
        val source = SourceEntity(
            id = "src-1",
            rssUrl = "https://example.com/rss",
            webUrl = "https://example.com",
            name = "Test Source",
            createdAt = 1000L,
            updatedAt = 1000L,
        )

        sourceDao.insert(source = source)

        val sources = sourceDao.getAll().first()
        assertEquals(expected = 1, actual = sources.size)
        assertEquals(expected = "src-1", actual = sources.first().id)
        assertEquals(expected = "Test Source", actual = sources.first().name)
    }

    @Test
    fun insertWithFeeds_savesSourceAndAssociatedFeeds() = runTest {
        val source = SourceEntity(
            id = "src-2",
            rssUrl = "https://example.com/rss2",
            webUrl = "https://example.com",
            name = "Test Source 2",
            createdAt = 2000L,
            updatedAt = 2000L,
        )
        val feed = FeedEntity(
            id = "feed-1",
            sourceId = "src-2",
            title = "Article 1",
            content = "Content 1",
            isRead = false,
            isSaved = false,
            url = "https://example.com/article1",
            date = 1500L,
            createdAt = 2000L,
            updatedAt = 2000L,
        )

        sourceDao.insertWithFeeds(
            source = source,
            feeds = listOf(feed),
        )

        val sources = sourceDao.getAll().first()
        val feeds = feedDao.getAll().first()

        assertEquals(expected = 1, actual = sources.size)
        assertEquals(expected = 1, actual = feeds.size)
        assertEquals(expected = "feed-1", actual = feeds.first().id)
        assertEquals(expected = "src-2", actual = feeds.first().sourceId)
        assertEquals(expected = "Article 1", actual = feeds.first().title)
        assertEquals(expected = "Content 1", actual = feeds.first().content)
    }

    @Test
    fun insertDuplicateSource_ignoresOnConflict() = runTest {
        val source1 = SourceEntity(
            id = "src-dup",
            rssUrl = "https://example.com/rss",
            webUrl = "https://example.com",
            name = "Original Name",
            createdAt = 1000L,
            updatedAt = 1000L,
        )
        val source2 = SourceEntity(
            id = "src-dup",
            rssUrl = "https://example.com/rss",
            webUrl = "https://example.com",
            name = "Updated Name",
            createdAt = 2000L,
            updatedAt = 2000L,
        )

        sourceDao.insert(source = source1)
        sourceDao.insert(source = source2)

        val sources = sourceDao.getAll().first()
        assertEquals(expected = 1, actual = sources.size)
        assertEquals(expected = "Original Name", actual = sources.first().name)
    }

    @Test
    fun insertFeeds_savesAndRetrievesFromFeedDao() = runTest {
        val source = SourceEntity(
            id = "src-feed-test",
            rssUrl = "https://example.com/rss",
            webUrl = "https://example.com",
            name = "Source for Feeds",
            createdAt = 1000L,
            updatedAt = 1000L,
        )
        sourceDao.insert(source = source)

        val feed1 = FeedEntity(
            id = "feed-a",
            sourceId = "src-feed-test",
            title = "Article A",
            content = "Body A",
            isRead = false,
            isSaved = false,
            url = "https://example.com/a",
            date = 1000L,
            createdAt = 1000L,
            updatedAt = 1000L,
        )
        val feed2 = FeedEntity(
            id = "feed-b",
            sourceId = "src-feed-test",
            title = "Article B",
            content = "Body B",
            isRead = true,
            isSaved = true,
            url = "https://example.com/b",
            date = 2000L,
            createdAt = 2000L,
            updatedAt = 2000L,
        )

        feedDao.insert(feeds = listOf(feed1, feed2))

        val feeds = feedDao.getAll().first()
        assertEquals(expected = 2, actual = feeds.size)
        assertEquals(expected = "feed-b", actual = feeds[0].id)
        assertEquals(expected = "Body B", actual = feeds[0].content)
        assertEquals(expected = "feed-a", actual = feeds[1].id)
        assertEquals(expected = "Body A", actual = feeds[1].content)
    }

    @Test
    fun setAsRead_marksFeedAsReadAndPendingSync() = runTest {
        val source = SourceEntity(
            id = "src-sync-test",
            rssUrl = "https://example.com/rss",
            webUrl = "https://example.com",
            name = "Source for Sync Test",
            createdAt = 1000L,
            updatedAt = 1000L,
        )
        sourceDao.insert(source = source)

        val feed = FeedEntity(
            id = "feed-sync-1",
            sourceId = "src-sync-test",
            title = "Article",
            content = "Body",
            isRead = false,
            isSaved = false,
            url = "https://example.com/1",
            date = 1000L,
            createdAt = 1000L,
            updatedAt = 1000L,
            isPendingSync = false,
        )
        feedDao.insert(feeds = listOf(feed))

        assertEquals(expected = emptyList(), actual = feedDao.getPendingSyncFeedIds())

        val rows = feedDao.setAsRead(id = "feed-sync-1")
        assertEquals(expected = 1, actual = rows)

        assertEquals(
            expected = listOf("feed-sync-1"),
            actual = feedDao.getPendingSyncFeedIds(),
        )

        feedDao.setAsSynced(id = "feed-sync-1")
        assertEquals(expected = emptyList(), actual = feedDao.getPendingSyncFeedIds())
    }

    @Test
    fun setAsSynced_withListOfIds_clearsPendingSyncForMultipleFeeds() = runTest {
        val source = SourceEntity(
            id = "src-sync-multi",
            rssUrl = "https://example.com/rss",
            webUrl = "https://example.com",
            name = "Source Multi",
            createdAt = 1000L,
            updatedAt = 1000L,
        )
        sourceDao.insert(source = source)

        val feed1 = FeedEntity(
            id = "feed-m-1",
            sourceId = "src-sync-multi",
            title = "A1",
            content = "B1",
            isRead = false,
            isSaved = false,
            url = "https://example.com/1",
            date = 1000L,
            createdAt = 1000L,
            updatedAt = 1000L,
        )
        val feed2 = FeedEntity(
            id = "feed-m-2",
            sourceId = "src-sync-multi",
            title = "A2",
            content = "B2",
            isRead = false,
            isSaved = false,
            url = "https://example.com/2",
            date = 1000L,
            createdAt = 1000L,
            updatedAt = 1000L,
        )
        feedDao.insert(feeds = listOf(feed1, feed2))

        feedDao.setAsRead(id = "feed-m-1")
        feedDao.setAsRead(id = "feed-m-2")

        val pending = feedDao.getPendingSyncFeedIds()
        assertEquals(expected = 2, actual = pending.size)

        feedDao.setAsSynced(ids = listOf("feed-m-1", "feed-m-2"))
        assertEquals(expected = emptyList(), actual = feedDao.getPendingSyncFeedIds())
    }
}
