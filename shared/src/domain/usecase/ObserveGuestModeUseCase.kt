package com.jjcr11.rss.domain.usecase

import com.jjcr11.rss.domain.repository.PreferencesRepository

import kotlinx.coroutines.flow.Flow

class ObserveGuestModeUseCase(
    private val repository: PreferencesRepository,
) {
    operator fun invoke(): Flow<Boolean> = repository.isGuestMode
}
