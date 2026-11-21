package com.jjcr11.rss.data.remote.datasource

import com.jjcr11.rss.data.remote.dto.SourceXmlDto
import com.jjcr11.rss.data.remote.parser.RssXmlParser
import io.ktor.client.HttpClient
import io.ktor.client.request.get
import io.ktor.client.statement.bodyAsText

class RssRemoteDataSource(
    private val httpClient: HttpClient,
    private val rssXmlParser: RssXmlParser,
) {
    suspend fun fetchRss(url: String): SourceXmlDto {
        val response = httpClient.get(urlString = url)
        val xmlContent = response.bodyAsText()
        return rssXmlParser.parse(xml = xmlContent)
    }
}
