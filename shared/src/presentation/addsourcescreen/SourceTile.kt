package com.jjcr11.rss.presentation.addsourcescreen

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.jjcr11.rss.presentation.components.DeleteIcon
import com.jjcr11.rss.presentation.theme.RssTheme

private fun extractInitials(name: String): String {
    val words = name.trim().split(regex = "\\s+".toRegex()).filter { word ->
        word.isNotEmpty()
    }
    return when {
        words.size >= 2 -> "${words[0].first()}${words[1].first()}".uppercase()
        (words.size == 1) && words[0].isNotEmpty() -> words[0].take(n = 2).uppercase()
        else -> ""
    }
}

@Composable
fun SourceTile(
    name: String,
    url: String,
    modifier: Modifier = Modifier,
    initials: String = extractInitials(name = name),
    onDeleteClick: () -> Unit = {},
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(
                horizontal = 16.dp,
                vertical = 12.dp,
            ),
        horizontalArrangement = Arrangement.spacedBy(space = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier = Modifier
                .size(size = 40.dp)
                .background(
                    color = MaterialTheme.colorScheme.surfaceContainerHigh,
                    shape = RoundedCornerShape(size = 8.dp),
                ),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = initials,
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary,
            )
        }

        Column(
            modifier = Modifier.weight(weight = 1f),
            verticalArrangement = Arrangement.spacedBy(space = 2.dp),
        ) {
            Text(
                text = name,
                style = MaterialTheme.typography.headlineSmall,
                color = MaterialTheme.colorScheme.primary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Text(
                text = url,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }

        IconButton(onClick = onDeleteClick) {
            Icon(
                imageVector = DeleteIcon,
                contentDescription = "Delete $name",
                tint = MaterialTheme.colorScheme.error,
            )
        }
    }
}

@Composable
@Preview
fun SourceTilePreview() {
    RssTheme {
        SourceTile(
            name = "Stripe Press",
            url = "press.stripe.com",
        )
    }
}

@Composable
@Preview
fun SourceTileDarkPreview() {
    RssTheme(
        darkTheme = true,
        content = {
            SourceTile(
                name = "Stripe Press",
                url = "press.stripe.com",
            )
        },
    )
}
