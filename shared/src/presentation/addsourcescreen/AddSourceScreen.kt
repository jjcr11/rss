package com.jjcr11.rss.presentation.addsourcescreen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
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
import com.jjcr11.rss.presentation.components.AddIcon
import com.jjcr11.rss.presentation.components.CloseIcon
import com.jjcr11.rss.presentation.components.RssFeedIcon
import com.jjcr11.rss.presentation.theme.RssTheme

@Composable
fun AddSourceScreen(
    modifier: Modifier = Modifier,
    onAddFeedClick: (String) -> Unit = {},
) {
    var url by remember { mutableStateOf(value = "") }

    Scaffold(
        modifier = modifier,
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues = innerPadding)
                .padding(all = 16.dp),
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
                                contentDescription = "Clear",
                                modifier = Modifier.size(size = 18.dp),
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                    }
                },
                singleLine = true,
            )

            Button(
                onClick = {
                    onAddFeedClick(url)
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(height = 48.dp),
                shape = RoundedCornerShape(size = 8.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary,
                ),
            ) {
                Icon(
                    imageVector = AddIcon,
                    contentDescription = null,
                    modifier = Modifier.size(size = 20.dp),
                )
                Spacer(modifier = Modifier.width(width = 8.dp))
                Text(
                    text = "ADD FEED",
                    style = MaterialTheme.typography.headlineSmall,
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
