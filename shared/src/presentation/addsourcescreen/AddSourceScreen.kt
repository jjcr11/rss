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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
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
import com.jjcr11.rss.presentation.components.CloseIcon
import com.jjcr11.rss.presentation.components.RssFeedIcon
import com.jjcr11.rss.presentation.theme.RssTheme
import com.jjcr11.rss.resources.Res
import com.jjcr11.rss.resources.add_feed_button
import com.jjcr11.rss.resources.clear_input
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun AddSourceRoute(
    modifier: Modifier = Modifier,
    viewModel: AddSourceViewModel = koinViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    AddSourceScreen(
        modifier = modifier,
        uiState = uiState,
        onAddFeedClick = { url ->
            viewModel.onEvent(event = AddSourceUiEvent.AddFeed(url = url))
        },
    )
}

@Composable
fun AddSourceScreen(
    modifier: Modifier = Modifier,
    uiState: AddSourceUiState = AddSourceUiState(),
    onAddFeedClick: (String) -> Unit = {},
    onDeleteSourceClick: (SourceUiModel) -> Unit = {},
) {
    var url by remember { mutableStateOf(value = "") }

    Scaffold(
        modifier = modifier,
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
                    OutlinedTextField(
                        value = url,
                        onValueChange = { newUrl ->
                            url = newUrl
                        },
                        modifier = Modifier.fillMaxWidth(),
                        textStyle = MaterialTheme.typography.labelMedium,
                        shape = RoundedCornerShape(size = 8.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = MaterialTheme.colorScheme.surfaceContainerLowest,
                            unfocusedContainerColor = MaterialTheme.colorScheme.surfaceContainerLowest,
                            focusedBorderColor = MaterialTheme.colorScheme.primary,
                            unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant,
                        ),
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
                        singleLine = true,
                    )

                    uiState.errorMessage?.let { errorMessage ->
                        Text(
                            text = errorMessage,
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.labelSmall,
                        )
                    }

                    Button(
                        onClick = {
                            onAddFeedClick(url)
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(height = 48.dp),
                        enabled = !uiState.isLoading,
                        shape = RoundedCornerShape(size = 8.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary,
                            contentColor = MaterialTheme.colorScheme.onPrimary,
                        ),
                    ) {
                        if (uiState.isLoading) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(size = 20.dp),
                                color = MaterialTheme.colorScheme.onPrimary,
                                strokeWidth = 2.dp,
                            )
                        } else {
                            Icon(
                                imageVector = AddIcon,
                                contentDescription = null,
                                modifier = Modifier.size(size = 20.dp),
                            )
                            Spacer(modifier = Modifier.width(width = 8.dp))
                            Text(
                                text = stringResource(resource = Res.string.add_feed_button),
                                style = MaterialTheme.typography.headlineSmall,
                            )
                        }
                    }
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
