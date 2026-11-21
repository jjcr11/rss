package com.jjcr11.rss

import androidx.compose.ui.window.ComposeUIViewController
import com.jjcr11.rss.di.initKoin

fun ViewController() = ComposeUIViewController {
    initKoin()
    App()
}
