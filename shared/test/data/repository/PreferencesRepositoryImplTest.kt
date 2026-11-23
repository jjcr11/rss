package com.jjcr11.rss.data.repository

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import com.jjcr11.rss.data.local.preferences.UserPreferencesDataSource
import com.jjcr11.rss.data.local.preferences.createDataStore
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue
import kotlin.uuid.Uuid

class PreferencesRepositoryImplTest {

    private fun createTestDataStore(): DataStore<Preferences> {
        return createDataStore(
            producePath = { "/tmp/test_pref_${Uuid.random()}.preferences_pb" },
        )
    }

    @Test
    fun isGuestMode_defaultsToFalse() = runTest {
        val dataStore = createTestDataStore()
        val dataSource = UserPreferencesDataSource(dataStore = dataStore)
        val repository = PreferencesRepositoryImpl(userPreferencesDataSource = dataSource)

        val result = repository.isGuestMode.first()

        assertFalse(actual = result)
    }

    @Test
    fun setGuestMode_updatesIsGuestModeFlow() = runTest {
        val dataStore = createTestDataStore()
        val dataSource = UserPreferencesDataSource(dataStore = dataStore)
        val repository = PreferencesRepositoryImpl(userPreferencesDataSource = dataSource)

        repository.setGuestMode(isGuestMode = true)

        val result = repository.isGuestMode.first()
        assertTrue(actual = result)
    }
}
