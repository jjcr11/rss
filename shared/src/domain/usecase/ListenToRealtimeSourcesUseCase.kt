package com.jjcr11.rss.domain.usecase

import com.jjcr11.rss.domain.repository.SourceRepository

class ListenToRealtimeSourcesUseCase(
    private val repository: SourceRepository,
) {
    suspend operator fun invoke() = repository.listenToRealtimeSources()
}
