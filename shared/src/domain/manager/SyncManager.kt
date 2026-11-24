package com.jjcr11.rss.domain.manager

import co.touchlab.kermit.Logger
import com.jjcr11.rss.domain.usecase.FetchFeedsFromSourcesUseCase
import com.jjcr11.rss.domain.usecase.SyncFeedsUseCase
import com.jjcr11.rss.domain.usecase.SyncSourcesUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.sync.Mutex
import kotlin.coroutines.cancellation.CancellationException

open class SyncManager(
    private val syncSourcesUseCase: SyncSourcesUseCase,
    private val syncFeedsUseCase: SyncFeedsUseCase,
    private val fetchFeedsFromSourcesUseCase: FetchFeedsFromSourcesUseCase? = null,
) {
    private val mutex = Mutex()
    private val _isSyncing = MutableStateFlow(value = false)
    open val isSyncing: StateFlow<Boolean> = _isSyncing.asStateFlow()

    open suspend fun sync() {
        if (!mutex.tryLock()) {
            return
        }
        _isSyncing.value = true
        try {
            syncSourcesUseCase()
            syncFeedsUseCase()
            fetchFeedsFromSourcesUseCase?.invoke()
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Logger.withTag(tag = TAG).w(throwable = e) {
                "Sync failed (offline mode)"
            }
        } finally {
            _isSyncing.value = false
            mutex.unlock()
        }
    }

    private companion object {
        const val TAG = "SyncManager"
    }
}
