package com.jjcr11.rss.data.remote.datasource

import com.jjcr11.rss.data.remote.dto.SourceDto
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.postgrest.from

open class SupabaseRemoteDataSource(
    private val supabaseClient: SupabaseClient,
) {
    open suspend fun insertSources(sources: List<SourceDto>) {
        supabaseClient.from(table = SOURCES_TABLE)
            .insert(values = sources)
    }

    open suspend fun getSources(): List<SourceDto> {
        return supabaseClient.from(table = SOURCES_TABLE)
            .select()
            .decodeList()
    }

    private companion object {
        const val SOURCES_TABLE = "sources"
    }
}
