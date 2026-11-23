package com.jjcr11.rss.domain.usecase

import com.jjcr11.rss.domain.repository.FeedRepository

class FetchFeedsFromSourcesUseCase(
    private val repository: FeedRepository,
) {
    suspend operator fun invoke() = repository.fetchFeedsFromSources()
}
