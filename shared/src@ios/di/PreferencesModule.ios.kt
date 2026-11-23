package com.jjcr11.rss.di

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import com.jjcr11.rss.data.local.preferences.DATA_STORE_FILE_NAME
import com.jjcr11.rss.data.local.preferences.createDataStore
import kotlinx.cinterop.ExperimentalForeignApi
import org.koin.core.module.Module
import org.koin.dsl.module
import platform.Foundation.NSDocumentDirectory
import platform.Foundation.NSFileManager
import platform.Foundation.NSUserDomainMask

@OptIn(ExperimentalForeignApi::class)
actual val platformPreferencesModule: Module = module {
    single<DataStore<Preferences>> {
        createDataStore(
            producePath = {
                val documentDirectory = NSFileManager.defaultManager.URLForDirectory(
                    directory = NSDocumentDirectory,
                    inDomain = NSUserDomainMask,
                    appropriateForURL = null,
                    create = true,
                    error = null,
                )
                requireNotNull(documentDirectory?.path) + "/$DATA_STORE_FILE_NAME"
            },
        )
    }
}
