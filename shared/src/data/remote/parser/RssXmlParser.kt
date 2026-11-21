package com.jjcr11.rss.data.remote.parser

import com.fleeksoft.ksoup.Ksoup
import com.fleeksoft.ksoup.nodes.Element
import com.fleeksoft.ksoup.parser.Parser
import com.jjcr11.rss.data.remote.dto.FeedXmlDto
import com.jjcr11.rss.data.remote.dto.SourceXmlDto

class RssXmlParser {

    fun parse(xml: String): SourceXmlDto {
        val document = Ksoup.parse(
            html = xml,
            parser = Parser.xmlParser(),
        )

        val channel = document.selectFirst("channel")
            ?: throw IllegalArgumentException("Invalid RSS feed: <channel> element not found")

        val title = channel.selectFirst("title")?.text().orEmpty()
        val link = channel.selectFirst("link")?.text().orEmpty()
        val description = channel.selectFirst("description")?.text()
        val language = channel.selectFirst("language")?.text()
        val lastBuildDate = channel.selectFirst("lastBuildDate")?.text()
        val updatePeriod = channel.selectFirst("sy|updatePeriod, updatePeriod")?.text()?.trim()
        val updateFrequency = channel.selectFirst("sy|updateFrequency, updateFrequency")?.text()?.trim()

        val feeds = channel.select("item").map { itemElement ->
            parseItem(item = itemElement)
        }

        return SourceXmlDto(
            title = title,
            link = link,
            description = description,
            language = language,
            lastBuildDate = lastBuildDate,
            updatePeriod = updatePeriod,
            updateFrequency = updateFrequency,
            feeds = feeds,
        )
    }

    private fun parseItem(item: Element): FeedXmlDto {
        val title = item.selectFirst("title")?.text().orEmpty()
        val link = item.selectFirst("link")?.text().orEmpty()
        val pubDate = item.selectFirst("pubDate")?.text()
        val description = item.selectFirst("description")?.text()

        val guidElement = item.selectFirst("guid")
        val guid = guidElement?.text()
        val isPermaLink = guidElement?.attr("isPermaLink")?.toBooleanStrictOrNull() ?: false

        val author = item.selectFirst("dc|creator, creator, author")?.text()
        val commentsUrl = item.selectFirst("comments")?.text()
        val commentCount = item.selectFirst("slash|comments")?.text()?.toIntOrNull()

        val categories = item.select("category").map { it.text() }

        val imageUrl = item.selectFirst("media|content")?.attr("url")
            ?: item.selectFirst("media|thumbnail")?.attr("url")
            ?: item.selectFirst("enclosure[type^=image]")?.attr("url")

        return FeedXmlDto(
            title = title,
            link = link,
            pubDate = pubDate,
            description = description,
            guid = guid,
            isPermaLink = isPermaLink,
            author = author,
            commentsUrl = commentsUrl,
            commentCount = commentCount,
            categories = categories,
            imageUrl = imageUrl,
        )
    }
}
