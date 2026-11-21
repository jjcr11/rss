package com.jjcr11.rss

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.jjcr11.rss.presentation.navigation.RssNavHost
import com.jjcr11.rss.presentation.theme.RssTheme
import org.jetbrains.compose.reload.DevelopmentEntryPoint

@Composable
@Preview
@DevelopmentEntryPoint
fun App() {
    RssTheme {
        Surface(modifier = Modifier.fillMaxSize()) {
            RssNavHost()
        }
    }
}
