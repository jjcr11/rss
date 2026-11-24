package com.jjcr11.rss.presentation.feedsscreen

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.VerticalDivider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.jjcr11.rss.presentation.addsourcescreen.AddSourcePanel
import com.jjcr11.rss.presentation.components.AddIcon
import com.jjcr11.rss.presentation.components.LocalScreenSize
import com.jjcr11.rss.presentation.components.SettingsIcon
import com.jjcr11.rss.presentation.theme.RssTheme
import com.jjcr11.rss.resources.Res
import com.jjcr11.rss.resources.add_feed_action
import com.jjcr11.rss.resources.feeds_title
import com.jjcr11.rss.resources.settings_title
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun FeedsRoute(
    modifier: Modifier = Modifier,
    viewModel: FeedsViewModel = koinViewModel(),
    onAddFeedClick: () -> Unit = {},
    onSettingsClick: () -> Unit = {},
    onFeedClick: (FeedUiModel) -> Unit = {},
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    FeedsScreen(
        modifier = modifier,
        uiState = uiState,
        onAddFeedClick = onAddFeedClick,
        onSettingsClick = onSettingsClick,
        onFeedClick = onFeedClick,
    )
}

@Composable
fun FeedsScreen(
    modifier: Modifier = Modifier,
    uiState: FeedsUiState = FeedsUiState(),
    onAddFeedClick: () -> Unit = {},
    onSettingsClick: () -> Unit = {},
    onFeedClick: (FeedUiModel) -> Unit = {},
) {
    val screenSize = LocalScreenSize.current

    Row(modifier = modifier.fillMaxSize()) {
        if (screenSize.isExpanded) {
            AddSourcePanel(
                modifier = Modifier
                    .width(width = 380.dp)
                    .fillMaxHeight(),
            )
            VerticalDivider(
                color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f),
            )
        }

        Scaffold(
            modifier = Modifier
                .weight(weight = 1f)
                .fillMaxHeight(),
            topBar = {
                TopAppBar(
                    title = {
                        Text(text = stringResource(resource = Res.string.feeds_title))
                    },
                    actions = {
                        if (!screenSize.isExpanded) {
                            IconButton(onClick = onAddFeedClick) {
                                Icon(
                                    imageVector = AddIcon,
                                    contentDescription = stringResource(resource = Res.string.add_feed_action),
                                )
                            }
                        }
                        IconButton(onClick = onSettingsClick) {
                            Icon(
                                imageVector = SettingsIcon,
                                contentDescription = stringResource(resource = Res.string.settings_title),
                            )
                        }
                    },
                )
            },
        ) { innerPadding ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues = innerPadding),
            ) {
                if (uiState.isLoading && uiState.feeds.isEmpty()) {
                    CircularProgressIndicator(
                        modifier = Modifier.align(alignment = Alignment.Center),
                    )
                } else {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                    ) {
                        items(
                            items = uiState.feeds,
                            key = { feed -> feed.id },
                        ) { feed ->
                            FeedTile(
                                sourceName = feed.sourceName,
                                title = feed.title,
                                date = feed.date,
                                isRead = feed.isRead,
                                onClick = { onFeedClick(feed) },
                            )
                            HorizontalDivider(
                                color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f),
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
@Preview
fun FeedsScreenPreview() {
    RssTheme {
        FeedsScreen(
            uiState = FeedsUiState(
                feeds = listOf(
                    FeedUiModel(
                        id = "1",
                        sourceName = "The Verge",
                        title = "Apple Silicon M4 Max Benchmarks Reveal Unprecedented Memory Bandwidth Gains",
                        date = "Nov 4, 2024",
                        url = "https://theverge.com",
                    ),
                    FeedUiModel(
                        id = "2",
                        sourceName = "Hacker News",
                        title = "Show HN: A Pure C99 Implementation of the Raft Consensus Engine",
                        date = "Nov 4, 2024",
                        url = "https://news.ycombinator.com",
                    ),
                ),
            ),
        )
    }
}

@Composable
@Preview
fun FeedsScreenDarkPreview() {
    RssTheme(
        darkTheme = true,
        content = {
            FeedsScreen(
                uiState = FeedsUiState(
                    feeds = listOf(
                        FeedUiModel(
                            id = "1",
                            sourceName = "The Verge",
                            title = "Apple Silicon M4 Max Benchmarks Reveal Unprecedented Memory Bandwidth Gains",
                            date = "Nov 4, 2024",
                            url = "https://theverge.com",
                        ),
                    ),
                ),
            )
        },
    )
}
