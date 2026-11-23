package com.jjcr11.rss.domain.repository

import kotlinx.coroutines.flow.Flow

interface PreferencesRepository {
    val isGuestMode: Flow<Boolean>
    suspend fun setGuestMode(isGuestMode: Boolean)
}
