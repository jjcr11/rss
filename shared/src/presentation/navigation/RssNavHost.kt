package com.jjcr11.rss.presentation.navigation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.jjcr11.rss.domain.model.AuthState
import com.jjcr11.rss.presentation.addsourcescreen.AddSourceRoute
import com.jjcr11.rss.presentation.authscreen.AuthRoute
import com.jjcr11.rss.presentation.feeddetail.FeedDetailRoute
import com.jjcr11.rss.presentation.feedsscreen.FeedsRoute
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun RssNavHost(
    modifier: Modifier = Modifier,
    navController: NavHostController = rememberNavController(),
    viewModel: NavigationViewModel = koinViewModel(),
) {
    val authState by viewModel.authState.collectAsStateWithLifecycle()

    if (authState == AuthState.Initializing) {
        Box(
            modifier = modifier.fillMaxSize(),
            contentAlignment = Alignment.Center,
        ) {
            CircularProgressIndicator(
                modifier = Modifier.size(size = 48.dp),
                color = MaterialTheme.colorScheme.primary,
                strokeWidth = 3.dp,
            )
        }
    } else {
        val startDestination = remember {
            if (authState is AuthState.Authenticated) {
                Screen.Feeds.route
            } else {
                Screen.Auth.route
            }
        }

        NavHost(
            navController = navController,
            startDestination = startDestination,
            modifier = modifier,
        ) {
            composable(route = Screen.Auth.route) {
                AuthRoute(
                    onAuthSuccess = {
                        navController.navigate(route = Screen.Feeds.route) {
                            popUpTo(route = Screen.Auth.route) {
                                inclusive = true
                            }
                        }
                    },
                    onContinueWithoutAccountClick = {
                        navController.navigate(route = Screen.Feeds.route) {
                            popUpTo(route = Screen.Auth.route) {
                                inclusive = true
                            }
                        }
                    },
                )
            }
            composable(route = Screen.Feeds.route) {
                FeedsRoute(
                    onAddFeedClick = {
                        navController.navigate(route = Screen.AddSource.route)
                    },
                    onFeedClick = { feed ->
                        navController.currentBackStackEntry?.savedStateHandle?.set(
                            key = "feed_title",
                            value = feed.title,
                        )
                        navController.currentBackStackEntry?.savedStateHandle?.set(
                            key = "feed_source_name",
                            value = feed.sourceName,
                        )
                        navController.currentBackStackEntry?.savedStateHandle?.set(
                            key = "feed_date",
                            value = feed.date,
                        )
                        navController.currentBackStackEntry?.savedStateHandle?.set(
                            key = "feed_content",
                            value = feed.content,
                        )
                        navController.navigate(route = Screen.FeedDetail.route)
                    },
                )
            }
            composable(route = Screen.AddSource.route) {
                AddSourceRoute(
                    onBackClick = {
                        navController.popBackStack()
                    },
                )
            }
            composable(route = Screen.FeedDetail.route) {
                val prevBackStackEntry = navController.previousBackStackEntry
                val title = prevBackStackEntry?.savedStateHandle?.get<String>(key = "feed_title").orEmpty()
                val sourceName = prevBackStackEntry?.savedStateHandle?.get<String>(key = "feed_source_name").orEmpty()
                val date = prevBackStackEntry?.savedStateHandle?.get<String>(key = "feed_date").orEmpty()
                val content = prevBackStackEntry?.savedStateHandle?.get<String>(key = "feed_content").orEmpty()

                FeedDetailRoute(
                    title = title,
                    sourceName = sourceName,
                    date = date,
                    content = content,
                    onBackClick = {
                        navController.popBackStack()
                    },
                )
            }
        }
    }
}
