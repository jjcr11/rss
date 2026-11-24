package com.jjcr11.rss

import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.jjcr11.rss.presentation.components.LocalScreenSize
import com.jjcr11.rss.presentation.components.ScreenSize
import com.jjcr11.rss.presentation.navigation.RssNavHost
import com.jjcr11.rss.presentation.theme.RssTheme
import org.jetbrains.compose.reload.DevelopmentEntryPoint

@Composable
@Preview
@DevelopmentEntryPoint
fun App() {
    RssTheme {
        BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
            val screenSize = ScreenSize(
                width = maxWidth,
                height = maxHeight,
            )
            CompositionLocalProvider(LocalScreenSize provides screenSize) {
                Surface(modifier = Modifier.fillMaxSize()) {
                    RssNavHost()
                }
            }
        }
    }
}
