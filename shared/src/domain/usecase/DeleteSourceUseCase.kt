package com.jjcr11.rss.domain.usecase

import com.jjcr11.rss.domain.model.Source
import com.jjcr11.rss.domain.repository.SourceRepository

class DeleteSourceUseCase(
    private val repository: SourceRepository,
) {
    suspend operator fun invoke(source: Source): Boolean =
        repository.delete(source = source)
}
