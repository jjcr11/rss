package com.jjcr11.rss.presentation.addsourcescreen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.jjcr11.rss.presentation.components.AddIcon
import com.jjcr11.rss.presentation.components.ArrowBackIcon
import com.jjcr11.rss.presentation.components.CloseIcon
import com.jjcr11.rss.presentation.components.RssButton
import com.jjcr11.rss.presentation.components.RssFeedIcon
import com.jjcr11.rss.presentation.components.RssTextField
import com.jjcr11.rss.presentation.theme.RssTheme
import com.jjcr11.rss.resources.Res
import com.jjcr11.rss.resources.add_feed_button
import com.jjcr11.rss.resources.add_source_title
import com.jjcr11.rss.resources.clear_input
import com.jjcr11.rss.resources.navigate_back
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun AddSourceRoute(
    modifier: Modifier = Modifier,
    viewModel: AddSourceViewModel = koinViewModel(),
    onBackClick: () -> Unit = {},
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    AddSourceScreen(
        modifier = modifier,
        uiState = uiState,
        onAddFeedClick = { url ->
            viewModel.onEvent(event = AddSourceUiEvent.AddFeed(url = url))
        },
        onDeleteSourceClick = { source ->
            viewModel.onEvent(event = AddSourceUiEvent.DeleteSource(source = source))
        },
        onBackClick = onBackClick,
    )
}

@Composable
fun AddSourceScreen(
    modifier: Modifier = Modifier,
    uiState: AddSourceUiState = AddSourceUiState(),
    onAddFeedClick: (String) -> Unit = {},
    onDeleteSourceClick: (SourceUiModel) -> Unit = {},
    onBackClick: () -> Unit = {},
) {
    var url by remember { mutableStateOf(value = "") }

    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = {
                    Text(text = stringResource(resource = Res.string.add_source_title))
                },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            imageVector = ArrowBackIcon,
                            contentDescription = stringResource(resource = Res.string.navigate_back),
                        )
                    }
                },
            )
        },
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues = innerPadding),
            contentPadding = PaddingValues(vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(space = 4.dp),
        ) {
            item {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(space = 16.dp),
                ) {
                    RssTextField(
                        value = url,
                        onValueChange = { newUrl ->
                            url = newUrl
                        },
                        leadingIcon = {
                            Icon(
                                imageVector = RssFeedIcon,
                                contentDescription = null,
                                modifier = Modifier.size(size = 18.dp),
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        },
                        trailingIcon = {
                            if (url.isNotEmpty()) {
                                IconButton(
                                    onClick = {
                                        url = ""
                                    },
                                ) {
                                    Icon(
                                        imageVector = CloseIcon,
                                        contentDescription = stringResource(resource = Res.string.clear_input),
                                        modifier = Modifier.size(size = 18.dp),
                                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                    )
                                }
                            }
                        },
                    )

                    uiState.errorMessage?.let { errorMessage ->
                        Text(
                            text = errorMessage,
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.labelSmall,
                        )
                    }

                    RssButton(
                        text = stringResource(resource = Res.string.add_feed_button),
                        onClick = {
                            onAddFeedClick(url)
                        },
                        isLoading = uiState.isLoading,
                        leadingIcon = AddIcon,
                    )
                }
            }

            item {
                Spacer(modifier = Modifier.height(height = 12.dp))
            }

            items(
                items = uiState.sources,
                key = { source -> source.id },
            ) { source ->
                SourceTile(
                    name = source.name,
                    url = source.url,
                    onDeleteClick = {
                        onDeleteSourceClick(source)
                    },
                )
                HorizontalDivider(
                    color = MaterialTheme.colorScheme.surfaceContainerLow,
                    thickness = 1.dp,
                )
            }
        }
    }
}

@Composable
@Preview
fun AddSourceScreenPreview() {
    RssTheme {
        AddSourceScreen()
    }
}

@Composable
@Preview
fun AddSourceScreenDarkPreview() {
    RssTheme(
        darkTheme = true,
        content = {
            AddSourceScreen()
        },
    )
}
