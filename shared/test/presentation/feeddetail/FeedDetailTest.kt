package com.jjcr11.rss.presentation.feeddetail

import androidx.compose.ui.text.LinkAnnotation
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertTrue

class FeedDetailTest {

    @Test
    fun parseHtmlToBlocks_withParagraphs_returnsCleanParagraphs() {
        val html = "<p>First paragraph with <b>bold text</b>.</p><p>Second paragraph with <a href=\"#\">link</a>.</p>"

        val blocks = parseHtmlToBlocks(html = html)

        assertEquals(expected = 2, actual = blocks.size)
        val first = assertIs<ArticleBlock.Text>(value = blocks[0])
        val second = assertIs<ArticleBlock.Text>(value = blocks[1])
        assertEquals(expected = "First paragraph with bold text.", actual = first.content)
        assertEquals(expected = "Second paragraph with link.", actual = second.content)
    }

    @Test
    fun parseHtmlToBlocks_withAnchorTags_preservesLinkAnnotation() {
        val html = "<p>Visit our <a href=\"https://example.com/guide\">guide</a> for more details.</p>"

        val blocks = parseHtmlToBlocks(html = html)

        assertEquals(expected = 1, actual = blocks.size)
        val textBlock = assertIs<ArticleBlock.Text>(value = blocks[0])
        assertEquals(expected = "Visit our guide for more details.", actual = textBlock.content)

        val annotatedString = textBlock.text
        val linkAnnotations = annotatedString.getLinkAnnotations(
            start = 0,
            end = annotatedString.length,
        )
        assertEquals(expected = 1, actual = linkAnnotations.size)
        val urlAnnotation = linkAnnotations.first().item
        val url = assertIs<LinkAnnotation.Url>(value = urlAnnotation)
        assertEquals(expected = "https://example.com/guide", actual = url.url)
    }

    @Test
    fun parseHtmlToBlocks_withImagesAndFigures_extractsImagesWithCaptions() {
        val html = """
            <p>Introductory paragraph.</p>
            <figure>
                <img src="https://example.com/schematic.png" alt="Fallback alt" />
                <figcaption>FIGURE 1.1: Distributed topology map</figcaption>
            </figure>
            <p><img src="https://example.com/diagram.jpg" alt="A diagram" />Post-image paragraph.</p>
        """.trimIndent()

        val blocks = parseHtmlToBlocks(html = html)

        assertEquals(expected = 4, actual = blocks.size)

        val block0 = assertIs<ArticleBlock.Text>(value = blocks[0])
        assertEquals(expected = "Introductory paragraph.", actual = block0.content)

        val block1 = assertIs<ArticleBlock.Image>(value = blocks[1])
        assertEquals(expected = "https://example.com/schematic.png", actual = block1.url)
        assertEquals(expected = "FIGURE 1.1: Distributed topology map", actual = block1.caption)

        val block2 = assertIs<ArticleBlock.Image>(value = blocks[2])
        assertEquals(expected = "https://example.com/diagram.jpg", actual = block2.url)
        assertEquals(expected = "A diagram", actual = block2.caption)

        val block3 = assertIs<ArticleBlock.Text>(value = blocks[3])
        assertEquals(expected = "Post-image paragraph.", actual = block3.content)
    }

    @Test
    fun parseHtmlToBlocks_withHeadings_marksHeadingFlag() {
        val html = "<h2>Subheading Title</h2><p>Body paragraph.</p>"

        val blocks = parseHtmlToBlocks(html = html)

        assertEquals(expected = 2, actual = blocks.size)
        val heading = assertIs<ArticleBlock.Text>(value = blocks[0])
        assertTrue(actual = heading.isHeading)
        assertEquals(expected = "Subheading Title", actual = heading.content)

        val body = assertIs<ArticleBlock.Text>(value = blocks[1])
        assertTrue(actual = !body.isHeading)
    }

    @Test
    fun parseHtmlToBlocks_withScriptAndStyleTags_stripsThemCleanly() {
        val html = "<script>alert('xss');</script><style>.body { color: red; }</style><p>Valid content</p>"

        val blocks = parseHtmlToBlocks(html = html)

        assertEquals(expected = 1, actual = blocks.size)
        val block = assertIs<ArticleBlock.Text>(value = blocks[0])
        assertEquals(expected = "Valid content", actual = block.content)
    }

    @Test
    fun parseHtmlToBlocks_withHtmlEntities_decodesEntities() {
        val html = "<p>Design &amp; Architecture &mdash; &quot;High Density&quot;</p>"

        val blocks = parseHtmlToBlocks(html = html)

        assertEquals(expected = 1, actual = blocks.size)
        val block = assertIs<ArticleBlock.Text>(value = blocks[0])
        assertEquals(expected = "Design & Architecture — \"High Density\"", actual = block.content)
    }

    @Test
    fun parseHtmlToBlocks_withEmptyOrBlankHtml_returnsEmptyList() {
        assertTrue(actual = parseHtmlToBlocks(html = "").isEmpty())
        assertTrue(actual = parseHtmlToBlocks(html = "   ").isEmpty())
    }

    @Test
    fun parseHtmlToBlocks_withoutBlockTags_splitsByNewlines() {
        val plainText = "Paragraph 1\n\nParagraph 2"

        val blocks = parseHtmlToBlocks(html = plainText)

        assertEquals(expected = 2, actual = blocks.size)
        val first = assertIs<ArticleBlock.Text>(value = blocks[0])
        val second = assertIs<ArticleBlock.Text>(value = blocks[1])
        assertEquals(expected = "Paragraph 1", actual = first.content)
        assertEquals(expected = "Paragraph 2", actual = second.content)
    }
}
