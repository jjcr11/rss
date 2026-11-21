package com.jjcr11.rss.di

import com.jjcr11.rss.data.remote.datasource.RssRemoteDataSource
import com.jjcr11.rss.data.remote.parser.RssXmlParser
import io.ktor.client.HttpClient
import org.koin.dsl.module

val networkModule = module {
    single { HttpClient() }
    single { RssXmlParser() }
    single {
        RssRemoteDataSource(
            httpClient = get(),
            rssXmlParser = get(),
        )
    }
}
