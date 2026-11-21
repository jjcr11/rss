package com.jjcr11.rss.domain.usecase

import com.jjcr11.rss.domain.model.Source
import com.jjcr11.rss.domain.repository.SourceRepository

class AddSourceUseCase(
    private val repository: SourceRepository,
) {
    suspend operator fun invoke(url: String): Source = repository.fetchAndSave(url = url)
}
