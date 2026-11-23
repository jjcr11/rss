package com.jjcr11.rss.presentation.feeddetail

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.LinkAnnotation
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextLinkStyles
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.withLink
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import com.fleeksoft.ksoup.Ksoup
import com.fleeksoft.ksoup.nodes.Element
import com.fleeksoft.ksoup.nodes.Node
import com.fleeksoft.ksoup.nodes.TextNode
import com.jjcr11.rss.presentation.components.ArrowBackIcon
import com.jjcr11.rss.presentation.theme.RssTheme
import com.jjcr11.rss.resources.Res
import com.jjcr11.rss.resources.feed_detail_kicker
import com.jjcr11.rss.resources.navigate_back
import org.jetbrains.compose.resources.stringResource

sealed interface ArticleBlock {
    data class Text(
        val content: String,
        val annotatedText: AnnotatedString? = null,
        val isHeading: Boolean = false,
    ) : ArticleBlock {
        val text: AnnotatedString
            get() = annotatedText ?: AnnotatedString(text = content)
    }

    data class Image(
        val url: String,
        val caption: String? = null,
    ) : ArticleBlock
}

@Composable
fun FeedDetailRoute(
    title: String,
    sourceName: String,
    date: String,
    content: String,
    modifier: Modifier = Modifier,
    onBackClick: () -> Unit = {},
) {
    FeedDetail(
        title = title,
        sourceName = sourceName,
        date = date,
        content = content,
        modifier = modifier,
        onBackClick = onBackClick,
    )
}

@Composable
fun FeedDetail(
    title: String,
    sourceName: String,
    date: String,
    content: String,
    modifier: Modifier = Modifier,
    onBackClick: () -> Unit = {},
) {
    val blocks = remember(key1 = content) {
        parseHtmlToBlocks(html = content)
    }

    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = {},
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            imageVector = ArrowBackIcon,
                            contentDescription = stringResource(resource = Res.string.navigate_back),
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                ),
            )
        },
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues = innerPadding)
                .verticalScroll(state = rememberScrollState())
                .padding(
                    horizontal = 16.dp,
                    vertical = 24.dp,
                ),
        ) {
            if (sourceName.isNotBlank() || date.isNotBlank()) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(space = 8.dp),
                ) {
                    Box(
                        modifier = Modifier
                            .size(size = 6.dp)
                            .background(
                                color = MaterialTheme.colorScheme.secondary,
                                shape = CircleShape,
                            ),
                    )
                    val kicker = if (date.isNotBlank()) {
                        stringResource(
                            resource = Res.string.feed_detail_kicker,
                            sourceName.uppercase(),
                            date.uppercase(),
                        )
                    } else {
                        sourceName.uppercase()
                    }
                    Text(
                        text = kicker,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                Spacer(modifier = Modifier.height(height = 8.dp))
            }

            Text(
                text = title,
                style = MaterialTheme.typography.displaySmall,
                color = MaterialTheme.colorScheme.onSurface,
            )

            Spacer(modifier = Modifier.height(height = 16.dp))

            blocks.forEach { block ->
                when (block) {
                    is ArticleBlock.Text -> {
                        Text(
                            text = block.text,
                            style = if (block.isHeading) {
                                MaterialTheme.typography.titleMedium
                            } else {
                                MaterialTheme.typography.bodyMedium
                            },
                            color = MaterialTheme.colorScheme.onSurface,
                        )
                    }
                    is ArticleBlock.Image -> {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(shape = RoundedCornerShape(size = 4.dp))
                                .background(color = MaterialTheme.colorScheme.surfaceContainer),
                        ) {
                            AsyncImage(
                                model = block.url,
                                contentDescription = block.caption,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(color = MaterialTheme.colorScheme.surfaceContainerHigh),
                                contentScale = ContentScale.FillWidth,
                            )
                            if (!block.caption.isNullOrBlank()) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .background(color = MaterialTheme.colorScheme.surfaceContainerLow)
                                        .padding(
                                            horizontal = 12.dp,
                                            vertical = 8.dp,
                                        ),
                                ) {
                                    Text(
                                        text = block.caption,
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    )
                                }
                            }
                        }
                    }
                }
                Spacer(modifier = Modifier.height(height = 16.dp))
            }
        }
    }
}

internal fun parseHtmlToBlocks(html: String): List<ArticleBlock> {
    if (html.isBlank()) return emptyList()
    val document = Ksoup.parse(html = html)
    val body = document.body()

    body.select("script, style").remove()
    body.select("br").forEach { it.replaceWith(TextNode(text = "\n")) }

    val blocks = mutableListOf<ArticleBlock>()
    val elements = body.select("p, h1, h2, h3, h4, h5, h6, blockquote, li, figure, img")
    val processedImages = mutableSetOf<Element>()

    for (element in elements) {
        when (element.tagName()) {
            "figure" -> {
                val img = element.selectFirst("img")
                if (img != null) {
                    val src = img.absUrl("src").ifBlank { img.attr("src") }.trim()
                    if (src.isNotBlank()) {
                        processedImages.add(img)
                        val caption = element.selectFirst("figcaption")?.text()?.trim()?.ifBlank { null }
                            ?: img.attr("alt").trim().ifBlank { null }
                        blocks.add(
                            ArticleBlock.Image(
                                url = src,
                                caption = caption,
                            ),
                        )
                    }
                }
            }
            "img" -> {
                if (element !in processedImages && element.parents().none { it.tagName() == "figure" }) {
                    val src = element.absUrl("src").ifBlank { element.attr("src") }.trim()
                    if (src.isNotBlank()) {
                        processedImages.add(element)
                        val caption = element.attr("alt").trim().ifBlank { null }
                        blocks.add(
                            ArticleBlock.Image(
                                url = src,
                                caption = caption,
                            ),
                        )
                    }
                }
            }
            else -> {
                val imagesInside = element.select("img").filter { it !in processedImages }
                for (img in imagesInside) {
                    val src = img.absUrl("src").ifBlank { img.attr("src") }.trim()
                    if (src.isNotBlank()) {
                        processedImages.add(img)
                        val caption = img.attr("alt").trim().ifBlank { null }
                        blocks.add(
                            ArticleBlock.Image(
                                url = src,
                                caption = caption,
                            ),
                        )
                    }
                }

                val hasNestedBlocks = element.select("p, h1, h2, h3, h4, h5, h6, blockquote, li").size > 1
                if (!hasNestedBlocks) {
                    val text = element.text().trim()
                    if (text.isNotBlank()) {
                        val isHeading = element.tagName().startsWith(prefix = "h")
                        val annotatedText = parseElementToAnnotatedString(element = element)
                        blocks.add(
                            ArticleBlock.Text(
                                content = text,
                                annotatedText = annotatedText,
                                isHeading = isHeading,
                            ),
                        )
                    }
                }
            }
        }
    }

    if (blocks.isNotEmpty()) {
        return blocks
    }

    return body.wholeText().trim().split("\n\n", "\n")
        .map { it.trim() }
        .filter { it.isNotBlank() }
        .map { ArticleBlock.Text(content = it) }
}

internal fun parseElementToAnnotatedString(element: Element): AnnotatedString {
    return buildAnnotatedString {
        appendNode(node = element)
    }
}

private fun AnnotatedString.Builder.appendNode(node: Node) {
    when (node) {
        is TextNode -> {
            append(text = node.text())
        }
        is Element -> {
            when (node.tagName()) {
                "a" -> {
                    val href = node.absUrl("href").ifBlank { node.attr("href") }.trim()
                    if (href.isNotBlank()) {
                        val linkStyle = TextLinkStyles(
                            style = SpanStyle(
                                textDecoration = TextDecoration.Underline,
                            ),
                        )
                        withLink(
                            link = LinkAnnotation.Url(
                                url = href,
                                styles = linkStyle,
                            ),
                        ) {
                            node.childNodes().forEach { child ->
                                appendNode(node = child)
                            }
                        }
                    } else {
                        node.childNodes().forEach { child ->
                            appendNode(node = child)
                        }
                    }
                }
                "img" -> {
                    // Images inside inline flow handled separately if extracted as blocks
                }
                else -> {
                    node.childNodes().forEach { child ->
                        appendNode(node = child)
                    }
                }
            }
        }
    }
}

@Composable
@Preview
fun FeedDetailPreview() {
    RssTheme {
        FeedDetail(
            title = "The Architecture of Scientific Revolutions: Computing in the Age of High Density",
            sourceName = "Stripe Press",
            date = "Oct 24, 2024",
            content = "<p>The canonical model of compute distribution has long treated memory access as a localized hierarchy: L1 registers at sub-nanosecond scale, moving symmetrically outward to secondary cache, coherent NUMA nodes, and ultimately across network fabrics to cold block arrays.</p><figure><img src=\"https://example.com/schematic.png\" alt=\"Topological map\"/><figcaption>FIGURE 1.1: Distributed memory interconnect topology</figcaption></figure><p>Yet the emergence of optically bonded photonic interconnects has collapsed the traditional penalty curve.</p>",
        )
    }
}

@Composable
@Preview
fun FeedDetailDarkPreview() {
    RssTheme(
        darkTheme = true,
        content = {
            FeedDetail(
                title = "The Architecture of Scientific Revolutions: Computing in the Age of High Density",
                sourceName = "Stripe Press",
                date = "Oct 24, 2024",
                content = "<p>The canonical model of compute distribution has long treated memory access as a localized hierarchy: L1 registers at sub-nanosecond scale, moving symmetrically outward to secondary cache, coherent NUMA nodes, and ultimately across network fabrics to cold block arrays.</p><figure><img src=\"https://example.com/schematic.png\" alt=\"Topological map\"/><figcaption>FIGURE 1.1: Distributed memory interconnect topology</figcaption></figure><p>Yet the emergence of optically bonded photonic interconnects has collapsed the traditional penalty curve.</p>",
            )
        },
    )
}
