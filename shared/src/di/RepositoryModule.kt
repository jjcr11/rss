package com.jjcr11.rss.di

import com.jjcr11.rss.data.repository.SourceRepositoryImpl
import com.jjcr11.rss.domain.repository.SourceRepository
import org.koin.dsl.module

val repositoryModule = module {
    single<SourceRepository> {
        SourceRepositoryImpl(
            sourceDao = get(),
            rssRemoteDataSource = get(),
        )
    }
}
