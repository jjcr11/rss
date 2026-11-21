package com.jjcr11.rss.domain.usecase

import com.jjcr11.rss.domain.model.Feed
import com.jjcr11.rss.domain.repository.FeedRepository
import kotlinx.coroutines.flow.Flow

class GetFeedsUseCase(
    private val repository: FeedRepository,
) {
    operator fun invoke(): Flow<List<Feed>> = repository.getAll()
}
