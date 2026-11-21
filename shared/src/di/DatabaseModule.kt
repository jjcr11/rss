package com.jjcr11.rss.di

import com.jjcr11.rss.data.local.dao.FeedDao
import com.jjcr11.rss.data.local.dao.SourceDao
import com.jjcr11.rss.data.local.db.RssDatabase
import org.koin.core.module.Module
import org.koin.dsl.module

expect val platformDatabaseModule: Module

val databaseModule = module {
    includes(platformDatabaseModule)
    single<SourceDao> { get<RssDatabase>().sourceDao() }
    single<FeedDao> { get<RssDatabase>().feedDao() }
}
