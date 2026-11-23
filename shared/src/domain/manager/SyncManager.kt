package com.jjcr11.rss.domain.manager

import kotlinx.coroutines.flow.StateFlow

interface SyncManager {
    val isSyncing: StateFlow<Boolean>

    suspend fun sync()
}
