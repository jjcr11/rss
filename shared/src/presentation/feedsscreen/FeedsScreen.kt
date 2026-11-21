package com.jjcr11.rss.presentation.feedsscreen

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.jjcr11.rss.presentation.components.AddIcon
import com.jjcr11.rss.presentation.theme.RssTheme
import com.jjcr11.rss.resources.Res
import com.jjcr11.rss.resources.add_feed_action
import com.jjcr11.rss.resources.feeds_title
import org.jetbrains.compose.resources.stringResource

@Composable
fun FeedsScreen(
    modifier: Modifier = Modifier,
    onAddFeedClick: () -> Unit = {},
) {
    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = {
                    Text(text = stringResource(resource = Res.string.feeds_title))
                },
                actions = {
                    IconButton(onClick = onAddFeedClick) {
                        Icon(
                            imageVector = AddIcon,
                            contentDescription = stringResource(resource = Res.string.add_feed_action),
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
        )
    }
}

@Composable
@Preview
fun FeedsScreenPreview() {
    RssTheme {
        FeedsScreen()
    }
}

@Composable
@Preview
fun FeedsScreenDarkPreview() {
    RssTheme(
        darkTheme = true,
        content = {
            FeedsScreen()
        },
    )
}
