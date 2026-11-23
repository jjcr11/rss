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
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.jsonPrimitive

open class SupabaseRemoteDataSource(
    private val supabaseClient: SupabaseClient,
) {
    open suspend fun insertFeeds(feeds: List<FeedDto>) {
        if (feeds.isNotEmpty()) {
            supabaseClient.from(table = FEEDS_TABLE).insert(values = feeds)
        }
    }

    open suspend fun insertSourceWithFeeds(source: SourceDto, feeds: List<FeedDto>) {
        supabaseClient.from(table = SOURCES_TABLE).insert(value = source)
        insertFeeds(feeds = feeds)
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

    open suspend fun deleteSource(id: String) {
        supabaseClient
            .from(table = SOURCES_TABLE)
            .delete {
                filter {
                    eq(
                        column = "id",
                        value = id,
                    )
                }
            }
    }

    open suspend fun deleteSources(ids: List<String>) {
        supabaseClient
            .from(table = SOURCES_TABLE)
            .delete {
                filter {
                    isIn(
                        column = "id",
                        values = ids,
                    )
                }
            }
    }

    open suspend fun deleteFeeds(ids: List<String>) {
        if (ids.isNotEmpty()) {
            supabaseClient
                .from(table = FEEDS_TABLE)
                .delete {
                    filter {
                        isIn(
                            column = "id",
                            values = ids,
                        )
                    }
                }
        }
    }

    open fun observeSources(): Flow<RealtimeSourceEvent> =
        flow {
            val channel = supabaseClient.channel(channelId = "source-channel")
            val actionFlow = channel.postgresChangeFlow<PostgresAction>(schema = "public") { table = SOURCES_TABLE }
            channel.subscribe()
            try {
                actionFlow.collect { action ->
                    when (action) {
                        is PostgresAction.Delete -> {
                            val id = action.oldRecord["id"]?.jsonPrimitive?.contentOrNull
                            if (id != null) {
                                emit(RealtimeSourceEvent.Deleted(id = id))
                            }
                        }
                        is PostgresAction.Insert -> {
                            val source = action.decodeRecord<SourceDto>()
                            emit(RealtimeSourceEvent.Inserted(source = source))
                        }
                        else -> {}
                    }
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


sealed interface RealtimeSourceEvent {
    data class Inserted(val source: SourceDto) : RealtimeSourceEvent
    data class Deleted(val id: String) : RealtimeSourceEvent
}
