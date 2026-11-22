package com.jjcr11.rss.di

import com.jjcr11.rss.BuildConfig
import com.jjcr11.rss.data.remote.datasource.AuthRemoteDataSource
import com.jjcr11.rss.data.remote.datasource.RssRemoteDataSource
import com.jjcr11.rss.data.remote.datasource.SupabaseRemoteDataSource
import com.jjcr11.rss.data.remote.parser.RssXmlParser
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.auth.Auth
import io.github.jan.supabase.auth.MemoryCodeVerifierCache
import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.postgrest.Postgrest
import io.github.jan.supabase.realtime.Realtime
import io.ktor.client.HttpClient
import org.koin.dsl.module

val networkModule = module {
    includes(platformSessionModule)
    single { HttpClient() }
    single { RssXmlParser() }
    single {
        RssRemoteDataSource(
            httpClient = get(),
            rssXmlParser = get(),
        )
    }
    single<SupabaseClient> {
        createSupabaseClient(
            supabaseUrl = BuildConfig.SUPABASE_URL,
            supabaseKey = BuildConfig.SUPABASE_KEY,
        ) {
            install(plugin = Postgrest)
            install(plugin = Realtime)
            install(plugin = Auth) {
                sessionManager = get()
                codeVerifierCache = MemoryCodeVerifierCache()
            }
        }
    }
    single {
        SupabaseRemoteDataSource(
            supabaseClient = get(),
        )
    }
    single {
        AuthRemoteDataSource(
            supabaseClient = get(),
        )
    }
}
