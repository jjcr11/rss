package com.jjcr11.rss.domain.usecase

import com.jjcr11.rss.domain.model.Source
import com.jjcr11.rss.domain.repository.SourceRepository
import kotlinx.coroutines.flow.Flow

class GetSourcesUseCase(
    private val repository: SourceRepository,
) {
    operator fun invoke(): Flow<List<Source>> = repository.getAll()
}
