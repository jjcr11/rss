package com.jjcr11.rss.di

import com.jjcr11.rss.data.repository.AuthRepositoryImpl
import com.jjcr11.rss.data.repository.FeedRepositoryImpl
import com.jjcr11.rss.data.repository.PreferencesRepositoryImpl
import com.jjcr11.rss.data.repository.SourceRepositoryImpl
import com.jjcr11.rss.domain.repository.AuthRepository
import com.jjcr11.rss.domain.repository.FeedRepository
import com.jjcr11.rss.domain.repository.PreferencesRepository
import com.jjcr11.rss.domain.repository.SourceRepository
import org.koin.dsl.module

val repositoryModule = module {
    single<SourceRepository> {
        SourceRepositoryImpl(
            sourceDao = get(),
            rssRemoteDataSource = get(),
            supabaseRemoteDataSource = get(),
        )
    }
    single<FeedRepository> {
        FeedRepositoryImpl(
            feedDao = get(),
            supabaseRemoteDataSource = get(),
            sourceDao = get(),
            rssRemoteDataSource = get(),
        )
    }
    single<AuthRepository> {
        AuthRepositoryImpl(
            authRemoteDataSource = get(),
            sourceDao = get(),
            userPreferencesDataSource = get(),
        )
    }
    single<PreferencesRepository> {
        PreferencesRepositoryImpl(
            userPreferencesDataSource = get(),
        )
    }
}
