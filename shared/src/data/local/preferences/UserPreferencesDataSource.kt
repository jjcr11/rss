package com.jjcr11.rss.data.local.preferences

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class UserPreferencesDataSource(
    private val dataStore: DataStore<Preferences>,
) {
    private val isGuestModeKey = booleanPreferencesKey(name = "is_guest_mode")

    val isGuestMode: Flow<Boolean> = dataStore.data
        .map { preferences -> preferences[isGuestModeKey] ?: false }

    suspend fun setGuestMode(isGuestMode: Boolean) {
        dataStore.edit { preferences ->
            preferences[isGuestModeKey] = isGuestMode
        }
    }
}
