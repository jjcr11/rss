package com.jjcr11.rss.data.repository

import com.jjcr11.rss.data.local.preferences.UserPreferencesDataSource
import com.jjcr11.rss.domain.repository.PreferencesRepository
import kotlinx.coroutines.flow.Flow

class PreferencesRepositoryImpl(
    private val userPreferencesDataSource: UserPreferencesDataSource,
) : PreferencesRepository {
    override val isGuestMode: Flow<Boolean> = userPreferencesDataSource.isGuestMode

    override suspend fun setGuestMode(isGuestMode: Boolean) {
        userPreferencesDataSource.setGuestMode(isGuestMode = isGuestMode)
    }
}
