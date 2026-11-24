package com.jjcr11.rss.data.local.db

import androidx.room.Room
import androidx.sqlite.driver.bundled.BundledSQLiteDriver
import com.jjcr11.rss.data.local.dao.FeedDao
import com.jjcr11.rss.data.local.dao.SourceDao
import com.jjcr11.rss.data.local.entity.FeedEntity
import com.jjcr11.rss.data.local.entity.PendingDeletedSourceEntity
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
    fun insertSource_withDuplicateRssUrl_isIgnoredDueToUniqueIndex() = runTest {
        val source1 = SourceEntity(
            id = "src-dup-1",
            rssUrl = "https://example.com/unique-rss",
            webUrl = "https://example.com",
            name = "Test Source 1",
            createdAt = 1000L,
            updatedAt = 1000L,
        )
        val source2 = SourceEntity(
            id = "src-dup-2",
            rssUrl = "https://example.com/unique-rss",
            webUrl = "https://example.com/other",
            name = "Test Source 2",
            createdAt = 2000L,
            updatedAt = 2000L,
        )

        sourceDao.insert(source = source1)
        sourceDao.insert(source = source2)

        val sources = sourceDao.getAll().first()
        assertEquals(expected = 1, actual = sources.size)
        assertEquals(expected = "src-dup-1", actual = sources.first().id)
    }

    @Test
    fun deleteLocal_insertsIntoPendingDeletedSourcesAndDeletesSource() = runTest {
        val source = SourceEntity(
            id = "src-del-local",
            rssUrl = "https://example.com/rss-del",
            webUrl = "https://example.com",
            name = "Test Delete",
            createdAt = 1000L,
            updatedAt = 1000L,
        )
        sourceDao.insert(source = source)
        assertEquals(expected = 1, actual = sourceDao.getAll().first().size)

        val pending = PendingDeletedSourceEntity(id = "src-del-local")
        val rows = sourceDao.deleteLocal(
            source = source,
            pendingDeletedSource = pending,
        )

        assertEquals(expected = 1, actual = rows)
        assertEquals(expected = 0, actual = sourceDao.getAll().first().size)
        assertEquals(
            expected = listOf("src-del-local"),
            actual = sourceDao.getPendingDeletedSourceIds(),
        )
    }

    @Test
    fun deletePendingDeletedSourcesById_removesMatchingIds() = runTest {
        val pending1 = PendingDeletedSourceEntity(id = "p-1")
        val pending2 = PendingDeletedSourceEntity(id = "p-2")
        sourceDao.insertPendingDeletedSource(pendingDeletedSource = pending1)
        sourceDao.insertPendingDeletedSource(pendingDeletedSource = pending2)

        assertEquals(
            expected = listOf("p-1", "p-2"),
            actual = sourceDao.getPendingDeletedSourceIds(),
        )

        sourceDao.deletePendingDeletedSourcesById(ids = listOf("p-1"))

        assertEquals(
            expected = listOf("p-2"),
            actual = sourceDao.getPendingDeletedSourceIds(),
        )
    }

    @Test
    fun deleteSource_cascadesAndDeletesAssociatedFeeds() = runTest {
        val source = SourceEntity(
            id = "src-cascade",
            rssUrl = "https://example.com/rss-cascade",
            webUrl = "https://example.com",
            name = "Test Cascade",
            createdAt = 1000L,
            updatedAt = 1000L,
        )
        val feed = FeedEntity(
            id = "feed-cascade-1",
            sourceId = "src-cascade",
            title = "Article Cascade",
            content = "Content Cascade",
            isRead = false,
            isSaved = false,
            url = "https://example.com/cascade-1",
            date = 1000L,
            createdAt = 1000L,
            updatedAt = 1000L,
        )
        sourceDao.insertWithFeeds(
            source = source,
            feeds = listOf(feed),
        )
        assertEquals(expected = 1, actual = feedDao.getAll().first().size)

        sourceDao.delete(source = source)

        assertEquals(expected = 0, actual = sourceDao.getAll().first().size)
        assertEquals(expected = 0, actual = feedDao.getAll().first().size)
    }

    @Test
    fun deleteById_deletesSourceAndCascadesFeeds() = runTest {
        val source = SourceEntity(
            id = "src-del-by-id",
            rssUrl = "https://example.com/rss-del-id",
            webUrl = "https://example.com",
            name = "Test DeleteById",
            createdAt = 1000L,
            updatedAt = 1000L,
        )
        val feed = FeedEntity(
            id = "feed-del-by-id",
            sourceId = "src-del-by-id",
            title = "Article DeleteById",
            content = "Content DeleteById",
            isRead = false,
            isSaved = false,
            url = "https://example.com/del-by-id",
            date = 1000L,
            createdAt = 1000L,
            updatedAt = 1000L,
        )
        sourceDao.insertWithFeeds(
            source = source,
            feeds = listOf(feed),
        )
        assertEquals(expected = 1, actual = sourceDao.getAll().first().size)
        assertEquals(expected = 1, actual = feedDao.getAll().first().size)

        val rows = sourceDao.deleteById(id = "src-del-by-id")

        assertEquals(expected = 1, actual = rows)
        assertEquals(expected = 0, actual = sourceDao.getAll().first().size)
        assertEquals(expected = 0, actual = feedDao.getAll().first().size)
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

        assertEquals(expected = emptyList(), actual = feedDao.getPendingSyncIds())

        val rows = feedDao.setAsRead(id = "feed-sync-1")
        assertEquals(expected = 1, actual = rows)

        assertEquals(
            expected = listOf("feed-sync-1"),
            actual = feedDao.getPendingSyncIds(),
        )

        feedDao.setAsSynced(id = "feed-sync-1")
        assertEquals(expected = emptyList(), actual = feedDao.getPendingSyncIds())
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

        val pending = feedDao.getPendingSyncIds()
        assertEquals(expected = 2, actual = pending.size)

        feedDao.setAsSynced(ids = listOf("feed-m-1", "feed-m-2"))
        assertEquals(expected = emptyList(), actual = feedDao.getPendingSyncIds())
    }

    @Test
    fun getUrlsBySourceId_returnsOnlyUrlsForSpecifiedSource() = runTest {
        val source1 = SourceEntity(
            id = "src-urls-1",
            rssUrl = "https://example.com/rss1",
            webUrl = "https://example.com/1",
            name = "Source 1",
            createdAt = 1000L,
            updatedAt = 1000L,
        )
        val source2 = SourceEntity(
            id = "src-urls-2",
            rssUrl = "https://example.com/rss2",
            webUrl = "https://example.com/2",
            name = "Source 2",
            createdAt = 1000L,
            updatedAt = 1000L,
        )
        sourceDao.insert(sources = listOf(source1, source2))

        val feed1 = FeedEntity(
            id = "feed-u-1",
            sourceId = "src-urls-1",
            title = "Article 1",
            content = "Body 1",
            isRead = false,
            isSaved = false,
            url = "https://example.com/item1",
            date = 1000L,
            createdAt = 1000L,
            updatedAt = 1000L,
        )
        val feed2 = FeedEntity(
            id = "feed-u-2",
            sourceId = "src-urls-1",
            title = "Article 2",
            content = "Body 2",
            isRead = false,
            isSaved = false,
            url = "https://example.com/item2",
            date = 2000L,
            createdAt = 2000L,
            updatedAt = 2000L,
        )
        val feed3 = FeedEntity(
            id = "feed-u-3",
            sourceId = "src-urls-2",
            title = "Article 3",
            content = "Body 3",
            isRead = false,
            isSaved = false,
            url = "https://example.com/item3",
            date = 3000L,
            createdAt = 3000L,
            updatedAt = 3000L,
        )
        feedDao.insert(feeds = listOf(feed1, feed2, feed3))

        val urlsSource1 = feedDao.getUrlsBySourceId(sourceId = "src-urls-1")
        val urlsSource2 = feedDao.getUrlsBySourceId(sourceId = "src-urls-2")

        assertEquals(
            expected = listOf("https://example.com/item1", "https://example.com/item2"),
            actual = urlsSource1,
        )
        assertEquals(
            expected = listOf("https://example.com/item3"),
            actual = urlsSource2,
        )
    }

    @Test
    fun getReadFeedsBySourceId_returnsOnlyReadFeedsForSource() = runTest {
        val source = SourceEntity(
            id = "src-read-test",
            rssUrl = "https://example.com/rss",
            webUrl = "https://example.com",
            name = "Source Read Test",
            createdAt = 1000L,
            updatedAt = 1000L,
        )
        sourceDao.insert(source = source)

        val readFeed = FeedEntity(
            id = "feed-read",
            sourceId = "src-read-test",
            title = "Read Article",
            content = "Body",
            isRead = true,
            isSaved = false,
            url = "https://example.com/read",
            date = 1000L,
            createdAt = 1000L,
            updatedAt = 1000L,
        )
        val unreadFeed = FeedEntity(
            id = "feed-unread",
            sourceId = "src-read-test",
            title = "Unread Article",
            content = "Body",
            isRead = false,
            isSaved = false,
            url = "https://example.com/unread",
            date = 2000L,
            createdAt = 2000L,
            updatedAt = 2000L,
        )
        feedDao.insert(feeds = listOf(readFeed, unreadFeed))

        val readFeeds = feedDao.getReadFeedsBySourceId(sourceId = "src-read-test")

        assertEquals(
            expected = 1,
            actual = readFeeds.size,
        )
        assertEquals(
            expected = "feed-read",
            actual = readFeeds.first().id,
        )
    }

    @Test
    fun deleteByIds_deletesSpecifiedFeeds() = runTest {
        val source = SourceEntity(
            id = "src-del-by-ids",
            rssUrl = "https://example.com/rss",
            webUrl = "https://example.com",
            name = "Source Delete Test",
            createdAt = 1000L,
            updatedAt = 1000L,
        )
        sourceDao.insert(source = source)

        val feed1 = FeedEntity(
            id = "feed-del-1",
            sourceId = "src-del-by-ids",
            title = "Article 1",
            content = "Body 1",
            isRead = true,
            isSaved = false,
            url = "https://example.com/del1",
            date = 1000L,
            createdAt = 1000L,
            updatedAt = 1000L,
        )
        val feed2 = FeedEntity(
            id = "feed-del-2",
            sourceId = "src-del-by-ids",
            title = "Article 2",
            content = "Body 2",
            isRead = true,
            isSaved = false,
            url = "https://example.com/del2",
            date = 2000L,
            createdAt = 2000L,
            updatedAt = 2000L,
        )
        feedDao.insert(feeds = listOf(feed1, feed2))

        val deletedCount = feedDao.deleteByIds(ids = listOf("feed-del-1"))

        assertEquals(
            expected = 1,
            actual = deletedCount,
        )
        val remaining = feedDao.getAll().first()
        assertEquals(
            expected = 1,
            actual = remaining.size,
        )
        assertEquals(
            expected = "feed-del-2",
            actual = remaining.first().id,
        )
    }

    @Test
    fun clearAll_deletesAllSourcesFeedsAndPendingDeletedSources() = runTest {
        val source = SourceEntity(
            id = "src-clear-1",
            rssUrl = "https://example.com/rss",
            webUrl = "https://example.com",
            name = "Clear Source",
            createdAt = 1000L,
            updatedAt = 1000L,
        )
        val feed = FeedEntity(
            id = "feed-clear-1",
            sourceId = "src-clear-1",
            title = "Clear Feed",
            content = "Content",
            isRead = false,
            isSaved = false,
            url = "https://example.com/clear-1",
            date = 1000L,
            createdAt = 1000L,
            updatedAt = 1000L,
        )
        sourceDao.insertWithFeeds(source = source, feeds = listOf(feed))
        sourceDao.insertPendingDeletedSource(
            pendingDeletedSource = PendingDeletedSourceEntity(id = "src-pending-1"),
        )

        sourceDao.clearAll()

        assertEquals(expected = 0, actual = sourceDao.getAll().first().size)
        assertEquals(expected = 0, actual = sourceDao.getPendingDeletedSourceIds().size)
        assertEquals(expected = 0, actual = feedDao.getAll().first().size)
    }
}
