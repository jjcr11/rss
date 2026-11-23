package com.jjcr11.rss.data.local.preferences

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue
import kotlin.uuid.Uuid

class UserPreferencesDataSourceTest {

    private fun createTestDataStore(): DataStore<Preferences> {
        return createDataStore(
            producePath = { "/tmp/test_pref_${Uuid.random()}.preferences_pb" },
        )
    }

    @Test
    fun isGuestMode_initiallyEmitsFalse() = runTest {
        val dataStore = createTestDataStore()
        val dataSource = UserPreferencesDataSource(dataStore = dataStore)

        assertFalse(actual = dataSource.isGuestMode.first())
    }

    @Test
    fun setGuestMode_persistsValueAndUpdatesFlow() = runTest {
        val dataStore = createTestDataStore()
        val dataSource = UserPreferencesDataSource(dataStore = dataStore)

        dataSource.setGuestMode(isGuestMode = true)
        assertTrue(actual = dataSource.isGuestMode.first())

        dataSource.setGuestMode(isGuestMode = false)
        assertFalse(actual = dataSource.isGuestMode.first())
    }
}
