package com.jjcr11.rss.presentation.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.jjcr11.rss.presentation.addsourcescreen.AddSourceRoute
import com.jjcr11.rss.presentation.feedsscreen.FeedsRoute

@Composable
fun RssNavHost(
    modifier: Modifier = Modifier,
    navController: NavHostController = rememberNavController(),
) {
    NavHost(
        navController = navController,
        startDestination = Screen.Feeds.route,
        modifier = modifier,
    ) {
        composable(route = Screen.Feeds.route) {
            FeedsRoute(
                onAddFeedClick = {
                    navController.navigate(route = Screen.AddSource.route)
                },
            )
        }
        composable(route = Screen.AddSource.route) {
            AddSourceRoute()
        }
    }
}
