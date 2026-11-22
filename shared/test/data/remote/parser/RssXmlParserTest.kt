package com.jjcr11.rss.data.remote.parser

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse

class RssXmlParserTest {

    private val parser = RssXmlParser()

    @Test
    fun parse_parsesRssXmlIntoSourceXmlDtoAndFeedXmlDtos() {
        val xml = """
            <?xml version="1.0" encoding="UTF-8"?>
            <rss version="2.0"
                xmlns:content="http://purl.org/rss/1.0/modules/content/"
                xmlns:dc="http://purl.org/dc/elements/1.1/"
                xmlns:sy="http://purl.org/rss/1.0/modules/syndication/"
                xmlns:slash="http://purl.org/rss/1.0/modules/slash/"
                xmlns:media="http://search.yahoo.com/mrss/">
                <channel>
                    <title>Tech Blog</title>
                    <link>https://example.com/</link>
                    <description>Software engineering and technology news.</description>
                    <lastBuildDate>Sat, 21 Nov 2026 15:47:17 +0000</lastBuildDate>
                    <language>en-US</language>
                    <sy:updatePeriod>hourly</sy:updatePeriod>
                    <sy:updateFrequency>1</sy:updateFrequency>

                    <item>
                        <title>Kotlin Multiplatform in 2026</title>
                        <link>https://example.com/posts/kmp-2026</link>
                        <comments>https://example.com/posts/kmp-2026#comments</comments>
                        <dc:creator><![CDATA[Jane Doe]]></dc:creator>
                        <pubDate>Sat, 21 Nov 2026 13:00:31 +0000</pubDate>
                        <category><![CDATA[Kotlin]]></category>
                        <category><![CDATA[Mobile]]></category>
                        <guid isPermaLink="false">https://example.com/?p=101</guid>
                        <description><![CDATA[Exploring the latest in KMP development.]]></description>
                        <slash:comments>5</slash:comments>
                        <media:content url="https://example.com/images/kmp.jpg" medium="image" />
                    </item>
                </channel>
            </rss>
        """.trimIndent()

        val source = parser.parse(xml = xml)

        assertEquals(
            expected = "Tech Blog",
            actual = source.title,
        )
        assertEquals(
            expected = "https://example.com/",
            actual = source.link,
        )
        assertEquals(
            expected = "Software engineering and technology news.",
            actual = source.description,
        )
        assertEquals(
            expected = "en-US",
            actual = source.language,
        )
        assertEquals(
            expected = "Sat, 21 Nov 2026 15:47:17 +0000",
            actual = source.lastBuildDate,
        )
        assertEquals(
            expected = "hourly",
            actual = source.updatePeriod,
        )
        assertEquals(
            expected = "1",
            actual = source.updateFrequency,
        )
        assertEquals(
            expected = 1,
            actual = source.feeds.size,
        )

        val feed = source.feeds.first()
        assertEquals(
            expected = "Kotlin Multiplatform in 2026",
            actual = feed.title,
        )
        assertEquals(
            expected = "https://example.com/posts/kmp-2026",
            actual = feed.link,
        )
        assertEquals(
            expected = "Sat, 21 Nov 2026 13:00:31 +0000",
            actual = feed.pubDate,
        )
        assertEquals(
            expected = "Exploring the latest in KMP development.",
            actual = feed.description,
        )
        assertEquals(
            expected = "Exploring the latest in KMP development.",
            actual = feed.content,
        )
        assertEquals(
            expected = "https://example.com/?p=101",
            actual = feed.guid,
        )
        assertFalse(actual = feed.isPermaLink)
        assertEquals(
            expected = "Jane Doe",
            actual = feed.author,
        )
        assertEquals(
            expected = "https://example.com/posts/kmp-2026#comments",
            actual = feed.commentsUrl,
        )
        assertEquals(
            expected = 5,
            actual = feed.commentCount,
        )
        assertEquals(
            expected = listOf("Kotlin", "Mobile"),
            actual = feed.categories,
        )
        assertEquals(
            expected = "https://example.com/images/kmp.jpg",
            actual = feed.imageUrl,
        )
    }

    @Test
    fun parse_whenContentEncodedPresent_extractsContentEncodedAsContent() {
        val xml = """
            <?xml version="1.0" encoding="UTF-8"?>
            <rss version="2.0" xmlns:content="http://purl.org/rss/1.0/modules/content/">
                <channel>
                    <title>Tech Blog</title>
                    <link>https://example.com/</link>
                    <item>
                        <title>Post Title</title>
                        <link>https://example.com/post-1</link>
                        <description><![CDATA[Summary teaser]]></description>
                        <content:encoded><![CDATA[<p>Full article body</p>]]></content:encoded>
                    </item>
                </channel>
            </rss>
        """.trimIndent()

        val source = parser.parse(xml = xml)
        val feed = source.feeds.first()

        assertEquals(
            expected = "Summary teaser",
            actual = feed.description,
        )
        assertEquals(
            expected = "<p>Full article body</p>",
            actual = feed.content,
        )
    }
}
