package com.jjcr11.rss

import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import com.jjcr11.rss.di.initKoin

fun main() {
    initKoin()
    application {
        Window(
            onCloseRequest = ::exitApplication,
            title = "Rss",
        ) {
            App()
        }
    }
}
