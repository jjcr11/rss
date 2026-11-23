package com.jjcr11.rss.domain.usecase

import com.jjcr11.rss.domain.repository.FeedRepository
import kotlin.uuid.Uuid

class SetFeedAsReadUseCase(
    private val repository: FeedRepository,
) {
    suspend operator fun invoke(id: Uuid) = repository.setAsRead(id = id)
}

