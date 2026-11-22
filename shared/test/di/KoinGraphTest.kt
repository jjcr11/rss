package com.jjcr11.rss.di

import com.jjcr11.rss.data.local.dao.FeedDao
import com.jjcr11.rss.data.local.dao.SourceDao
import com.jjcr11.rss.data.local.entity.FeedEntity
import com.jjcr11.rss.data.local.entity.SourceEntity
import com.jjcr11.rss.data.remote.datasource.AuthRemoteDataSource
import com.jjcr11.rss.data.remote.datasource.RssRemoteDataSource
import com.jjcr11.rss.data.remote.datasource.SupabaseRemoteDataSource
import com.jjcr11.rss.data.remote.parser.RssXmlParser
import com.jjcr11.rss.domain.repository.AuthRepository
import com.jjcr11.rss.domain.repository.FeedRepository
import com.jjcr11.rss.domain.repository.SourceRepository
import com.jjcr11.rss.domain.usecase.AddSourceUseCase
import com.jjcr11.rss.domain.usecase.GetFeedsUseCase
import com.jjcr11.rss.domain.usecase.GetSourcesUseCase
import com.jjcr11.rss.domain.usecase.ListenToRealtimeSourcesUseCase
import com.jjcr11.rss.domain.usecase.ObserveAuthStateUseCase
import com.jjcr11.rss.domain.usecase.SignUpUseCase
import com.jjcr11.rss.domain.usecase.SyncSourcesUseCase
import com.jjcr11.rss.presentation.addsourcescreen.AddSourceViewModel
import com.jjcr11.rss.presentation.feedsscreen.FeedsViewModel
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.auth.SessionManager
import io.github.jan.supabase.auth.auth
import io.ktor.client.HttpClient
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import org.koin.dsl.koinApplication
import org.koin.dsl.module
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertNotNull

@OptIn(ExperimentalCoroutinesApi::class)
class KoinGraphTest {

    private val testDispatcher = StandardTestDispatcher()

    private val testDatabaseModule = module {
        single<SourceDao> {
            object : SourceDao {
                override suspend fun insert(source: SourceEntity) = Unit
                override suspend fun insert(sources: List<SourceEntity>) = Unit
                override suspend fun insertFeeds(feeds: List<FeedEntity>) = Unit
                override fun getAll(): Flow<List<SourceEntity>> = emptyFlow()
            }
        }
        single<FeedDao> {
            object : FeedDao {
                override fun getAll(): Flow<List<FeedEntity>> = emptyFlow()
            }
        }
    }

    private lateinit var koinApp: org.koin.core.KoinApplication

    @BeforeTest
    fun setUp() {
        Dispatchers.setMain(dispatcher = testDispatcher)
        koinApp = koinApplication(createEagerInstances = false) {
            modules(
                listOf(
                    networkModule,
                    testDatabaseModule,
                    repositoryModule,
                    useCaseModule,
                    presentationModule,
                ),
            )
        }
    }

    @AfterTest
    fun tearDown() {
        koinApp.close()
        Dispatchers.resetMain()
    }

    @Test
    fun verifyKoinGraph_resolvesAllDependenciesSuccessfully() {
        val koin = koinApp.koin

        // Infrastructure & Network
        assertNotNull(actual = koin.get<HttpClient>())
        assertNotNull(actual = koin.get<RssXmlParser>())
        val supabaseClient = koin.get<SupabaseClient>()
        assertNotNull(actual = supabaseClient)
        assertNotNull(actual = supabaseClient.auth)
        assertNotNull(actual = koin.get<SessionManager>())
        assertNotNull(actual = koin.get<RssRemoteDataSource>())
        assertNotNull(actual = koin.get<SupabaseRemoteDataSource>())
        assertNotNull(actual = koin.get<AuthRemoteDataSource>())

        // DAOs
        assertNotNull(actual = koin.get<SourceDao>())
        assertNotNull(actual = koin.get<FeedDao>())

        // Repositories
        assertNotNull(actual = koin.get<SourceRepository>())
        assertNotNull(actual = koin.get<FeedRepository>())
        assertNotNull(actual = koin.get<AuthRepository>())

        // Use Cases
        assertNotNull(actual = koin.get<AddSourceUseCase>())
        assertNotNull(actual = koin.get<GetSourcesUseCase>())
        assertNotNull(actual = koin.get<GetFeedsUseCase>())
        assertNotNull(actual = koin.get<SyncSourcesUseCase>())
        assertNotNull(actual = koin.get<ListenToRealtimeSourcesUseCase>())
        assertNotNull(actual = koin.get<SignUpUseCase>())
        assertNotNull(actual = koin.get<ObserveAuthStateUseCase>())

        // ViewModels
        assertNotNull(actual = koin.get<AddSourceViewModel>())
        assertNotNull(actual = koin.get<FeedsViewModel>())
    }
}
