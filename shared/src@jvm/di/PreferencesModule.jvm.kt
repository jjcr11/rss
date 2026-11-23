package com.jjcr11.rss.di

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import com.jjcr11.rss.data.local.preferences.DATA_STORE_FILE_NAME
import com.jjcr11.rss.data.local.preferences.createDataStore
import org.koin.core.module.Module
import org.koin.dsl.module
import java.io.File

actual val platformPreferencesModule: Module = module {
    single<DataStore<Preferences>> {
        createDataStore(
            producePath = {
                val dir = File("${System.getProperty("user.home")}/.rss")
                dir.mkdirs()
                File(dir, DATA_STORE_FILE_NAME).absolutePath
            },
        )
    }
}
