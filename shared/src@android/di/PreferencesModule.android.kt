package com.jjcr11.rss.di

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import com.jjcr11.rss.data.local.preferences.DATA_STORE_FILE_NAME
import com.jjcr11.rss.data.local.preferences.createDataStore
import org.koin.android.ext.koin.androidContext
import org.koin.core.module.Module
import org.koin.dsl.module

actual val platformPreferencesModule: Module = module {
    single<DataStore<Preferences>> {
        createDataStore(
            producePath = {
                androidContext().filesDir.resolve(DATA_STORE_FILE_NAME).absolutePath
            },
        )
    }
}
