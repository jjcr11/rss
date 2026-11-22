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
            read = false,
            save = false,
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
            read = false,
            save = false,
            url = "https://example.com/a",
            date = 1000L,
            createdAt = 1000L,
            updatedAt = 1000L,
        )
        val feed2 = FeedEntity(
            id = "feed-b",
            sourceId = "src-feed-test",
            title = "Article B",
            read = true,
            save = true,
            url = "https://example.com/b",
            date = 2000L,
            createdAt = 2000L,
            updatedAt = 2000L,
        )

        feedDao.insert(feeds = listOf(feed1, feed2))

        val feeds = feedDao.getAll().first()
        assertEquals(expected = 2, actual = feeds.size)
        assertEquals(expected = "feed-b", actual = feeds[0].id)
        assertEquals(expected = "feed-a", actual = feeds[1].id)
    }
}
