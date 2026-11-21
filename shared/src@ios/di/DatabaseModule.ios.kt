package com.jjcr11.rss.di

import com.jjcr11.rss.data.local.db.RssDatabase
import com.jjcr11.rss.data.local.db.getDatabaseBuilder
import com.jjcr11.rss.data.local.db.getRoomDatabase
import org.koin.core.module.Module
import org.koin.dsl.module

actual val platformDatabaseModule: Module = module {
    single<RssDatabase> {
        getRoomDatabase(
            builder = getDatabaseBuilder(),
        )
    }
}
