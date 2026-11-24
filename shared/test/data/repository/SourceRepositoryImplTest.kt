package com.jjcr11.rss.data.repository

import com.jjcr11.rss.data.local.dao.SourceDao
import com.jjcr11.rss.data.local.entity.FeedEntity
import com.jjcr11.rss.data.local.entity.PendingDeletedSourceEntity
import com.jjcr11.rss.data.local.entity.SourceEntity
import com.jjcr11.rss.data.remote.datasource.RealtimeSourceEvent
import com.jjcr11.rss.data.remote.datasource.RssRemoteDataSource
import com.jjcr11.rss.data.remote.datasource.SupabaseRemoteDataSource
import com.jjcr11.rss.data.remote.dto.FeedDto
import com.jjcr11.rss.data.remote.dto.SourceDto
import com.jjcr11.rss.data.remote.parser.RssXmlParser
import com.jjcr11.rss.domain.model.Source
import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.postgrest.Postgrest
import io.ktor.client.HttpClient
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.respond
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.http.headersOf
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import kotlin.time.Clock
import kotlin.uuid.Uuid

@OptIn(ExperimentalCoroutinesApi::class)
class SourceRepositoryImplTest {

    private class FakeSourceDao : SourceDao {
        val insertedSources = mutableListOf<SourceEntity>()
        val insertedFeeds = mutableListOf<FeedEntity>()
        val deletedSources = mutableListOf<SourceEntity>()
        val deletedSourceIds = mutableListOf<String>()
        val pendingDeletedSources = mutableListOf<PendingDeletedSourceEntity>()
        var sourcesFlow: Flow<List<SourceEntity>> = flowOf(emptyList())
        var deleteResult: Int = 1

        override suspend fun insert(source: SourceEntity) {
            insertedSources.add(element = source)
        }

        override suspend fun insert(sources: List<SourceEntity>) {
            insertedSources.addAll(elements = sources)
        }

        override suspend fun insertFeeds(feeds: List<FeedEntity>) {
            insertedFeeds.addAll(elements = feeds)
        }

        override fun getAll(): Flow<List<SourceEntity>> = sourcesFlow

        override suspend fun delete(source: SourceEntity): Int {
            deletedSources.add(element = source)
            return deleteResult
        }

        override suspend fun deleteFeedsBySourceId(sourceId: String): Int = 0

        override suspend fun deleteSourceById(id: String): Int = deleteResult

        override suspend fun deleteById(id: String): Int {
            deletedSourceIds.add(element = id)
            return deleteResult
        }

        override suspend fun insertPendingDeletedSource(pendingDeletedSource: PendingDeletedSourceEntity) {
            pendingDeletedSources.add(element = pendingDeletedSource)
        }

        override suspend fun getPendingDeletedSourceIds(): List<String> =
            pendingDeletedSources.map { it.id }

        override suspend fun deletePendingDeletedSource(pendingDeletedSource: PendingDeletedSourceEntity): Int {
            val removed = pendingDeletedSources.removeAll { it.id == pendingDeletedSource.id }
            return if (removed) 1 else 0
        }

        override suspend fun deletePendingDeletedSourcesById(ids: List<String>): Int {
            val count = pendingDeletedSources.count { it.id in ids }
            pendingDeletedSources.removeAll { it.id in ids }
            return count
        }

        override suspend fun deleteAll(): Int = 0

        override suspend fun deleteAllPendingDeleted(): Int = 0

        override suspend fun deleteAllFeeds(): Int = 0
    }

    private class FakeSupabaseRemoteDataSource : SupabaseRemoteDataSource(
        supabaseClient = createSupabaseClient(
            supabaseUrl = "https://example.supabase.co",
            supabaseKey = "anon",
        ) {
            install(plugin = Postgrest)
        },
    ) {
        val insertedSources = mutableListOf<SourceDto>()
        val insertedFeeds = mutableListOf<FeedDto>()
        val deletedSourceIds = mutableListOf<String>()
        val deletedSourcesBatch = mutableListOf<List<String>>()
        var sourcesToReturn: List<SourceDto> = emptyList()
        val sourcesFlow = MutableSharedFlow<RealtimeSourceEvent>(replay = 1)
        var deleteSourceShouldThrow: Exception? = null
        var deleteSourcesShouldThrow: Exception? = null

        override suspend fun insertSourceWithFeeds(source: SourceDto, feeds: List<FeedDto>) {
            insertedSources.add(element = source)
            insertedFeeds.addAll(elements = feeds)
        }

        override suspend fun getSources(): List<SourceDto> = sourcesToReturn

        override fun observeSources(): Flow<RealtimeSourceEvent> = sourcesFlow

        override suspend fun deleteSource(id: String) {
            deleteSourceShouldThrow?.let { throw it }
            deletedSourceIds.add(element = id)
        }

        override suspend fun deleteSources(ids: List<String>) {
            deleteSourcesShouldThrow?.let { throw it }
            deletedSourcesBatch.add(element = ids)
        }
    }

    private fun createRepository(
        dao: FakeSourceDao,
        supabaseDataSource: FakeSupabaseRemoteDataSource = FakeSupabaseRemoteDataSource(),
        xmlResponse: String = "",
    ): SourceRepositoryImpl {
        val mockEngine = MockEngine {
            respond(
                content = xmlResponse,
                status = HttpStatusCode.OK,
                headers = headersOf(HttpHeaders.ContentType, "application/xml"),
            )
        }
        val httpClient = HttpClient(engine = mockEngine)
        val rssDataSource = RssRemoteDataSource(
            httpClient = httpClient,
            rssXmlParser = RssXmlParser(),
        )
        return SourceRepositoryImpl(
            sourceDao = dao,
            rssRemoteDataSource = rssDataSource,
            supabaseRemoteDataSource = supabaseDataSource,
        )
    }

    private val sampleRssXml = """
        <?xml version="1.0" encoding="UTF-8"?>
        <rss version="2.0">
            <channel>
                <title>Test Feed Title</title>
                <link>https://example.com</link>
                <description>Test Description</description>
                <item>
                    <title>Item 1</title>
                    <link>https://example.com/item1</link>
                    <description>Item 1 Description</description>
                    <pubDate>Mon, 01 Jan 2024 12:00:00 GMT</pubDate>
                </item>
            </channel>
        </rss>
    """.trimIndent()

    @Test
    fun fetchAndSave_fetchesXmlSavesToDaoAndReplicatesToSupabase() = runTest {
        val dao = FakeSourceDao()
        val supabaseDataSource = FakeSupabaseRemoteDataSource()
        val repository = createRepository(
            dao = dao,
            supabaseDataSource = supabaseDataSource,
            xmlResponse = sampleRssXml,
        )

        val source = repository.fetchAndSave(url = "https://example.com/feed.xml")

        assertEquals(
            expected = "Test Feed Title",
            actual = source.name,
        )
        assertEquals(
            expected = "https://example.com/feed.xml",
            actual = source.rssUrl,
        )
        assertEquals(
            expected = "https://example.com",
            actual = source.webUrl,
        )
        assertEquals(
            expected = 1,
            actual = dao.insertedSources.size,
        )
        assertEquals(
            expected = 1,
            actual = dao.insertedFeeds.size,
        )
        assertEquals(
            expected = 1,
            actual = supabaseDataSource.insertedSources.size,
        )
        assertEquals(
            expected = 1,
            actual = supabaseDataSource.insertedFeeds.size,
        )
        assertEquals(
            expected = "Test Feed Title",
            actual = supabaseDataSource.insertedSources.first().name,
        )
        assertEquals(
            expected = "Item 1",
            actual = supabaseDataSource.insertedFeeds.first().title,
        )
        assertEquals(
            expected = "Item 1 Description",
            actual = dao.insertedFeeds.first().content,
        )
        assertEquals(
            expected = "Item 1 Description",
            actual = supabaseDataSource.insertedFeeds.first().content,
        )
    }

    @Test
    fun syncSources_fetchesFromSupabaseAndInsertsIntoDao() = runTest {
        val dao = FakeSourceDao()
        val supabaseDataSource = FakeSupabaseRemoteDataSource()
        val repository = createRepository(
            dao = dao,
            supabaseDataSource = supabaseDataSource,
        )
        val now = Clock.System.now()
        val remoteDto = SourceDto(
            id = Uuid.random().toString(),
            rssUrl = "https://example.com/feed.xml",
            webUrl = "https://example.com",
            name = "Remote Feed",
            createdAt = now.toEpochMilliseconds(),
            updatedAt = now.toEpochMilliseconds(),
        )
        supabaseDataSource.sourcesToReturn = listOf(remoteDto)

        repository.syncSources()

        assertEquals(
            expected = 1,
            actual = dao.insertedSources.size,
        )
        val entity = dao.insertedSources.first()
        assertEquals(
            expected = remoteDto.id,
            actual = entity.id,
        )
        assertEquals(
            expected = remoteDto.name,
            actual = entity.name,
        )
    }

    @Test
    fun listenToRealtimeSources_collectsFromSupabaseAndInsertsIntoDao() = runTest(context = UnconfinedTestDispatcher()) {
        val dao = FakeSourceDao()
        val supabaseDataSource = FakeSupabaseRemoteDataSource()
        val repository = createRepository(
            dao = dao,
            supabaseDataSource = supabaseDataSource,
        )
        val now = Clock.System.now()
        val remoteDto = SourceDto(
            id = Uuid.random().toString(),
            rssUrl = "https://example.com/feed.xml",
            webUrl = "https://example.com",
            name = "Realtime Feed",
            createdAt = now.toEpochMilliseconds(),
            updatedAt = now.toEpochMilliseconds(),
        )

        val job = backgroundScope.launch {
            repository.listenToRealtimeSources()
        }

        supabaseDataSource.sourcesFlow.emit(
            value = RealtimeSourceEvent.Inserted(source = remoteDto),
        )

        assertEquals(
            expected = 1,
            actual = dao.insertedSources.size,
        )
        val entity = dao.insertedSources.first()
        assertEquals(
            expected = remoteDto.id,
            actual = entity.id,
        )
        job.cancel()
    }

    @Test
    fun listenToRealtimeSources_whenDeletedEventReceived_deletesFromDaoById() = runTest(context = UnconfinedTestDispatcher()) {
        val dao = FakeSourceDao()
        val supabaseDataSource = FakeSupabaseRemoteDataSource()
        val repository = createRepository(
            dao = dao,
            supabaseDataSource = supabaseDataSource,
        )
        val sourceId = Uuid.random().toString()

        val job = backgroundScope.launch {
            repository.listenToRealtimeSources()
        }

        supabaseDataSource.sourcesFlow.emit(
            value = RealtimeSourceEvent.Deleted(id = sourceId),
        )

        assertEquals(
            expected = listOf(sourceId),
            actual = dao.deletedSourceIds,
        )
        job.cancel()
    }

    @Test
    fun getAll_mapsEntitiesToDomainAndEmits() = runTest {
        val dao = FakeSourceDao()
        val repository = createRepository(dao = dao)
        val id = Uuid.random()
        val now = Clock.System.now()
        val entity = SourceEntity(
            id = id.toString(),
            rssUrl = "https://example.com/feed.xml",
            webUrl = "https://example.com",
            name = "Test Feed",
            createdAt = now.toEpochMilliseconds(),
            updatedAt = now.toEpochMilliseconds(),
        )
        dao.sourcesFlow = flowOf(value = listOf(entity))

        val sources = repository.getAll().first()

        assertEquals(
            expected = 1,
            actual = sources.size,
        )
        val domain = sources.first()
        assertEquals(
            expected = id,
            actual = domain.id,
        )
        assertEquals(
            expected = "https://example.com/feed.xml",
            actual = domain.rssUrl,
        )
        assertEquals(
            expected = "https://example.com",
            actual = domain.webUrl,
        )
        assertEquals(
            expected = "Test Feed",
            actual = domain.name,
        )
    }

    @Test
    fun delete_whenSuccessful_deletesLocalAndRemoteAndCleansPending() = runTest {
        val dao = FakeSourceDao()
        val supabaseDataSource = FakeSupabaseRemoteDataSource()
        val repository = createRepository(
            dao = dao,
            supabaseDataSource = supabaseDataSource,
        )
        val id = Uuid.random()
        val now = Clock.System.now()
        val source = Source(
            id = id,
            rssUrl = "https://example.com/rss",
            webUrl = "https://example.com",
            name = "Test Source",
            createdAt = now,
            updatedAt = now,
        )

        val result = repository.delete(source = source)

        assertTrue(actual = result)
        assertEquals(
            expected = 1,
            actual = dao.deletedSources.size,
        )
        assertEquals(
            expected = id.toString(),
            actual = dao.deletedSources.first().id,
        )
        assertEquals(
            expected = listOf(id.toString()),
            actual = supabaseDataSource.deletedSourceIds,
        )
        assertTrue(actual = dao.pendingDeletedSources.isEmpty())
    }

    @Test
    fun delete_whenRemoteFails_deletesLocalAndKeepsInPending() = runTest {
        val dao = FakeSourceDao()
        val supabaseDataSource = FakeSupabaseRemoteDataSource()
        supabaseDataSource.deleteSourceShouldThrow = RuntimeException("Network offline")
        val repository = createRepository(
            dao = dao,
            supabaseDataSource = supabaseDataSource,
        )
        val id = Uuid.random()
        val now = Clock.System.now()
        val source = Source(
            id = id,
            rssUrl = "https://example.com/rss",
            webUrl = "https://example.com",
            name = "Test Source",
            createdAt = now,
            updatedAt = now,
        )

        val result = repository.delete(source = source)

        assertTrue(actual = result)
        assertEquals(
            expected = 1,
            actual = dao.deletedSources.size,
        )
        assertEquals(
            expected = 1,
            actual = dao.pendingDeletedSources.size,
        )
        assertEquals(
            expected = id.toString(),
            actual = dao.pendingDeletedSources.first().id,
        )
        assertTrue(actual = supabaseDataSource.deletedSourceIds.isEmpty())
    }

    @Test
    fun syncSources_whenPendingDeletedSourcesExist_deletesThemFromRemoteAndDao() = runTest {
        val dao = FakeSourceDao()
        val supabaseDataSource = FakeSupabaseRemoteDataSource()
        val repository = createRepository(
            dao = dao,
            supabaseDataSource = supabaseDataSource,
        )
        val pending1 = PendingDeletedSourceEntity(id = "src-del-1")
        val pending2 = PendingDeletedSourceEntity(id = "src-del-2")
        dao.insertPendingDeletedSource(pendingDeletedSource = pending1)
        dao.insertPendingDeletedSource(pendingDeletedSource = pending2)

        repository.syncSources()

        assertEquals(
            expected = 1,
            actual = supabaseDataSource.deletedSourcesBatch.size,
        )
        assertEquals(
            expected = listOf("src-del-1", "src-del-2"),
            actual = supabaseDataSource.deletedSourcesBatch.first(),
        )
        assertTrue(actual = dao.pendingDeletedSources.isEmpty())
    }

    private val wordPressRssXml = """
        <?xml version="1.0" encoding="UTF-8"?>
        <rss version="2.0"
            xmlns:content="http://purl.org/rss/1.0/modules/content/"
            xmlns:wfw="http://wellformedweb.org/CommentAPI/"
            xmlns:dc="http://purl.org/dc/elements/1.1/"
            xmlns:atom="http://www.w3.org/2005/Atom"
            xmlns:sy="http://purl.org/rss/1.0/modules/syndication/"
            xmlns:slash="http://purl.org/rss/1.0/modules/slash/">
            <channel>
                <title>Mientras Tanto en México</title>
                <atom:link href="https://www.mientrastantoenmexico.mx/feed/" rel="self" type="application/rss+xml" />
                <link>https://www.mientrastantoenmexico.mx</link>
                <description>Mientras tanto en México es un medio de comunicación digital.</description>
                <lastBuildDate>Tue, 27 Oct 2026 18:07:50 +0000</lastBuildDate>
                <language>es-MX</language>
                <sy:updatePeriod>hourly</sy:updatePeriod>
                <sy:updateFrequency>1</sy:updateFrequency>
                <generator>https://wordpress.org/?v=5.5.3</generator>
                <image>
                    <url>https://cdn.mientrastantoenmexico.mx/wp-content/uploads/2020/11/cropped-favicon-32x32.png</url>
                    <title>Mientras Tanto en México</title>
                    <link>https://www.mientrastantoenmexico.mx</link>
                    <width>32</width>
                    <height>32</height>
                </image>
                <item>
                    <title>Mejores ofertas del Buen Fin: qué revisar antes de elegir un equipo de cobro</title>
                    <link>https://www.mientrastantoenmexico.mx/mejores-ofertas-del-buen-fin-que-revisar-antes-de-elegir-un-equipo-de-cobro/</link>
                    <comments>https://www.mientrastantoenmexico.mx/mejores-ofertas-del-buen-fin-que-revisar-antes-de-elegir-un-equipo-de-cobro/#comments</comments>
                    <dc:creator><![CDATA[A1 MTEM]]></dc:creator>
                    <pubDate>Mon, 26 Oct 2026 19:44:24 +0000</pubDate>
                    <category><![CDATA[Tendencias]]></category>
                    <guid isPermaLink="false">https://www.mientrastantoenmexico.mx/?p=427596</guid>
                    <description><![CDATA[Las mejores ofertas del Buen Fin no siempre son las que muestran el beneficio más llamativo a primera vista.]]></description>
                    <content:encoded><![CDATA[<p>Las <strong>mejores ofertas del Buen Fin</strong> no siempre son las que muestran el beneficio más llamativo.</p>]]></content:encoded>
                    <wfw:commentRss>https://www.mientrastantoenmexico.mx/mejores-ofertas-del-buen-fin-que-revisar-antes-de-elegir-un-equipo-de-cobro/feed/</wfw:commentRss>
                    <slash:comments>1</slash:comments>
                </item>
                <item>
                    <title>Seguritech: certificaciones, cumplimiento y Misión Crítica sustentan su modelo operativo</title>
                    <link>https://www.mientrastantoenmexico.mx/seguritech-certificaciones-cumplimiento-mision-critica/</link>
                    <comments>https://www.mientrastantoenmexico.mx/seguritech-certificaciones-cumplimiento-mision-critica/#comments</comments>
                    <dc:creator><![CDATA[Victor Dorantes]]></dc:creator>
                    <pubDate>Tue, 06 Oct 2026 00:59:29 +0000</pubDate>
                    <category><![CDATA[Tendencias]]></category>
                    <category><![CDATA[centros c5]]></category>
                    <guid isPermaLink="false">https://www.mientrastantoenmexico.mx/?p=427588</guid>
                    <description><![CDATA[A través de auditorías independientes, alineación a normativas jurídicas y alianzas con líderes globales como Oracle.]]></description>
                    <content:encoded><![CDATA[<p>A través de auditorías independientes, alineación a normativas jurídicas.</p>]]></content:encoded>
                    <wfw:commentRss>https://www.mientrastantoenmexico.mx/seguritech-certificaciones-cumplimiento-mision-critica/feed/</wfw:commentRss>
                    <slash:comments>8</slash:comments>
                </item>
            </channel>
        </rss>
    """.trimIndent()

    @Test
    fun fetchAndSave_withWordPressRssXml_parsesAndSavesAllFeedsToDaoAndSupabase() = runTest {
        val dao = FakeSourceDao()
        val supabaseDataSource = FakeSupabaseRemoteDataSource()
        val repository = createRepository(
            dao = dao,
            supabaseDataSource = supabaseDataSource,
            xmlResponse = wordPressRssXml,
        )

        val source = repository.fetchAndSave(url = "https://www.mientrastantoenmexico.mx/feed/")

        assertEquals(
            expected = "Mientras Tanto en México",
            actual = source.name,
        )
        assertEquals(
            expected = "https://www.mientrastantoenmexico.mx/feed/",
            actual = source.rssUrl,
        )
        assertEquals(
            expected = "https://www.mientrastantoenmexico.mx",
            actual = source.webUrl,
        )
        assertEquals(
            expected = 1,
            actual = dao.insertedSources.size,
        )
        assertEquals(
            expected = 2,
            actual = dao.insertedFeeds.size,
        )
        assertEquals(
            expected = 2,
            actual = supabaseDataSource.insertedFeeds.size,
        )

        val firstFeed = dao.insertedFeeds.first()
        assertEquals(
            expected = "Mejores ofertas del Buen Fin: qué revisar antes de elegir un equipo de cobro",
            actual = firstFeed.title,
        )
        assertEquals(
            expected = "https://www.mientrastantoenmexico.mx/mejores-ofertas-del-buen-fin-que-revisar-antes-de-elegir-un-equipo-de-cobro/",
            actual = firstFeed.url,
        )
        assertEquals(
            expected = "<p>Las <strong>mejores ofertas del Buen Fin</strong> no siempre son las que muestran el beneficio más llamativo.</p>",
            actual = firstFeed.content,
        )
        assertEquals(
            expected = source.id.toString(),
            actual = firstFeed.sourceId,
        )
    }

    private val mediumRssXml = """
        <?xml version="1.0" encoding="UTF-8"?>
        <rss xmlns:dc="http://purl.org/dc/elements/1.1/" xmlns:content="http://purl.org/rss/1.0/modules/content/" xmlns:atom="http://www.w3.org/2005/Atom" xmlns:cc="http://cyber.law.harvard.edu/rss/creativeCommonsRssModule.html" version="2.0">
            <channel>
                <title>
                    <![CDATA[ Stories by Netflix Technology Blog on Medium ]]>
                </title>
                <description>
                    <![CDATA[ Stories by Netflix Technology Blog on Medium ]]>
                </description>
                <link>https://medium.com/@netflixtechblog?source=rss-c3aeaf49d8a4------2</link>
                <image>
                    <url>https://cdn-images-1.medium.com/fit/c/150/150/1*BJWRqfSMf9Da9vsXG9EBRQ.jpeg</url>
                    <title>Stories by Netflix Technology Blog on Medium</title>
                    <link>https://medium.com/@netflixtechblog?source=rss-c3aeaf49d8a4------2</link>
                </image>
                <generator>Medium</generator>
                <lastBuildDate>Sun, 22 Nov 2026 22:50:04 GMT</lastBuildDate>
                <atom:link href="https://medium.com/@netflixtechblog/feed" rel="self" type="application/rss+xml"/>
                <webMaster>
                    <![CDATA[ yourfriends@medium.com ]]>
                </webMaster>
                <atom:link href="http://medium.superfeedr.com" rel="hub"/>
                <item>
                    <title>
                        <![CDATA[ Engineering AI Discover ]]>
                    </title>
                    <link>https://netflixtechblog.com/engineering-ai-discover-e244944cdc38?source=rss-c3aeaf49d8a4------2</link>
                    <guid isPermaLink="false">https://medium.com/p/e244944cdc38</guid>
                    <category>
                        <![CDATA[ ai-agent ]]>
                    </category>
                    <category>
                        <![CDATA[ artificial-intelligence ]]>
                    </category>
                    <dc:creator>
                        <![CDATA[ Netflix Technology Blog ]]>
                    </dc:creator>
                    <pubDate>Tue, 17 Nov 2026 17:01:02 GMT</pubDate>
                    <atom:updated>2026-11-17T17:01:02.131Z</atom:updated>
                    <content:encoded>
                        <![CDATA[ <p>The challenges and decisions behind AI Discover’s intelligence layer</p> ]]>
                    </content:encoded>
                </item>
                <item>
                    <title>
                        <![CDATA[ Fact-Checking at Scale: Building a More Accurate and Efficient Synopsis Factuality System ]]>
                    </title>
                    <link>https://netflixtechblog.com/fact-checking-at-scale-building-a-more-accurate-and-efficient-synopsis-factuality-system-d7d85859d808?source=rss-c3aeaf49d8a4------2</link>
                    <guid isPermaLink="false">https://medium.com/p/d7d85859d808</guid>
                    <dc:creator>
                        <![CDATA[ Netflix Technology Blog ]]>
                    </dc:creator>
                    <pubDate>Mon, 09 Nov 2026 17:01:02 GMT</pubDate>
                    <atom:updated>2026-11-09T17:01:02.202Z</atom:updated>
                    <content:encoded>
                        <![CDATA[ <p>Synopsis factuality system</p> ]]>
                    </content:encoded>
                </item>
            </channel>
        </rss>
    """.trimIndent()

    @Test
    fun fetchAndSave_withMediumRssXml_parsesAndSavesAllFeedsToDaoAndSupabase() = runTest {
        val dao = FakeSourceDao()
        val supabaseDataSource = FakeSupabaseRemoteDataSource()
        val repository = createRepository(
            dao = dao,
            supabaseDataSource = supabaseDataSource,
            xmlResponse = mediumRssXml,
        )

        val source = repository.fetchAndSave(url = "https://medium.com/@netflixtechblog/feed")

        assertEquals(
            expected = "Stories by Netflix Technology Blog on Medium",
            actual = source.name,
        )
        assertEquals(
            expected = "https://medium.com/@netflixtechblog/feed",
            actual = source.rssUrl,
        )
        assertEquals(
            expected = "https://medium.com/@netflixtechblog?source=rss-c3aeaf49d8a4------2",
            actual = source.webUrl,
        )
        assertEquals(
            expected = 1,
            actual = dao.insertedSources.size,
        )
        assertEquals(
            expected = 2,
            actual = dao.insertedFeeds.size,
        )
        assertEquals(
            expected = 2,
            actual = supabaseDataSource.insertedFeeds.size,
        )

        val firstFeed = dao.insertedFeeds.first()
        assertEquals(
            expected = "Engineering AI Discover",
            actual = firstFeed.title,
        )
        assertEquals(
            expected = "https://netflixtechblog.com/engineering-ai-discover-e244944cdc38?source=rss-c3aeaf49d8a4------2",
            actual = firstFeed.url,
        )
        assertEquals(
            expected = "<p>The challenges and decisions behind AI Discover’s intelligence layer</p>",
            actual = firstFeed.content,
        )
        assertEquals(
            expected = source.id.toString(),
            actual = firstFeed.sourceId,
        )
    }
}
