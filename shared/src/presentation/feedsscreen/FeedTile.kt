package com.jjcr11.rss.presentation.feedsscreen

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.jjcr11.rss.presentation.theme.RssTheme

@Composable
fun FeedTile(
    sourceName: String,
    title: String,
    date: String,
    modifier: Modifier = Modifier,
    onClick: () -> Unit = {},
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(
                horizontal = 16.dp,
                vertical = 12.dp,
            ),
        verticalArrangement = Arrangement.spacedBy(space = 6.dp),
    ) {
        Text(
            text = sourceName.uppercase(),
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.secondary,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurface,
            maxLines = 3,
            overflow = TextOverflow.Ellipsis,
        )
        Text(
            text = date,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.outline,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
    }
}

@Composable
@Preview
fun FeedTilePreview() {
    RssTheme {
        FeedTile(
            sourceName = "MacRumors",
            title = "Apple Silicon M4 Max Benchmarks Reveal Unprecedented Memory Bandwidth Gains",
            date = "Nov 4, 2024",
        )
    }
}

@Composable
@Preview
fun FeedTileDarkPreview() {
    RssTheme(
        darkTheme = true,
        content = {
            FeedTile(
                sourceName = "MacRumors",
                title = "Apple Silicon M4 Max Benchmarks Reveal Unprecedented Memory Bandwidth Gains",
                date = "Nov 4, 2024",
            )
        },
    )
}
