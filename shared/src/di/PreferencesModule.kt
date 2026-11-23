package com.jjcr11.rss.di

import com.jjcr11.rss.data.local.preferences.UserPreferencesDataSource
import org.koin.core.module.Module
import org.koin.dsl.module

expect val platformPreferencesModule: Module

val preferencesModule = module {
    includes(platformPreferencesModule)
    single {
        UserPreferencesDataSource(
            dataStore = get(),
        )
    }
}
