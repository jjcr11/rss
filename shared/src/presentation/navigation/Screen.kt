package com.jjcr11.rss.presentation.navigation

sealed class Screen(val route: String) {
    data object Feeds : Screen(route = "feeds")
    data object AddSource : Screen(route = "add_source")
}
