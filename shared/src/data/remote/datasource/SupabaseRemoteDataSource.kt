package com.jjcr11.rss.data.remote.datasource

import com.jjcr11.rss.data.remote.dto.FeedDto
import com.jjcr11.rss.data.remote.dto.SourceDto
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.postgrest.from

open class SupabaseRemoteDataSource(
    private val supabaseClient: SupabaseClient,
) {
    open suspend fun insertSourceWithFeeds(source: SourceDto, feeds: List<FeedDto>) {
        supabaseClient.from(table = SOURCES_TABLE).insert(value = source)
        if (feeds.isNotEmpty()) {
            supabaseClient.from(table = FEEDS_TABLE).insert(values = feeds)
        }
    }

    private companion object {
        const val SOURCES_TABLE = "sources"
        const val FEEDS_TABLE = "feeds"
    }
}
