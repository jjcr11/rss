package com.jjcr11.rss.data.remote.datasource

import com.jjcr11.rss.data.remote.dto.FeedDto
import com.jjcr11.rss.data.remote.dto.SourceDto
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.realtime.PostgresAction
import io.github.jan.supabase.realtime.channel
import io.github.jan.supabase.realtime.decodeRecord
import io.github.jan.supabase.realtime.postgresChangeFlow
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.withContext

open class SupabaseRemoteDataSource(
    private val supabaseClient: SupabaseClient,
) {
    open suspend fun insertSourceWithFeeds(source: SourceDto, feeds: List<FeedDto>) {
        supabaseClient.from(table = SOURCES_TABLE).insert(value = source)
        if (feeds.isNotEmpty()) {
            supabaseClient.from(table = FEEDS_TABLE).insert(values = feeds)
        }
    }

    open suspend fun getSources(): List<SourceDto> =
        supabaseClient.from(table = SOURCES_TABLE)
            .select()
            .decodeList()

    open suspend fun getUnreadFeeds(): List<FeedDto> =
        supabaseClient.from(table = FEEDS_TABLE)
            .select {
                filter {
                    eq("is_read", false)
                }
            }
            .decodeList()

    open suspend fun setFeedAsRead(id: String) {
        supabaseClient
            .from(table = FEEDS_TABLE)
            .update(
                update = {
                    set(
                        column = "is_read",
                        value = true,
                    )
                },
            ) {
                filter {
                    eq(
                        column = "id",
                        value = id,
                    )
                }
            }
    }

    open suspend fun setFeedsAsRead(ids: List<String>) {
        supabaseClient
            .from(table = FEEDS_TABLE)
            .update(
                update = {
                    set(
                        column = "is_read",
                        value = true,
                    )
                },
            ) {
                filter {
                    isIn(
                        column = "id",
                        values = ids,
                    )
                }
            }
    }

    open fun observeSources(): Flow<SourceDto> =
        flow {
            val channel = supabaseClient.channel(channelId = "source-channel")
            val actionFlow = channel.postgresChangeFlow<PostgresAction.Insert>(schema = "public") {
                table = SOURCES_TABLE
            }
            channel.subscribe()
            try {
                actionFlow.collect { action ->
                    emit(action.decodeRecord<SourceDto>())
                }
            } finally {
                withContext(context = NonCancellable) {
                    try {
                        channel.unsubscribe()
                    } catch (_: Exception) {
                        // Ignore unsubscribe errors during cancellation
                    }
                }
            }
        }

    private companion object {
        const val SOURCES_TABLE = "sources"
        const val FEEDS_TABLE = "feeds"
    }
}
