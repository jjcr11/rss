package com.jjcr11.rss.domain.usecase

import com.jjcr11.rss.domain.repository.PreferencesRepository

class SetGuestModeUseCase(
    private val repository: PreferencesRepository,
) {
    suspend operator fun invoke(isGuestMode: Boolean) {
        repository.setGuestMode(isGuestMode = isGuestMode)
    }
}
