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
                )
            }
            composable(route = Screen.AddSource.route) {
                AddSourceRoute(
                    onBackClick = {
                        navController.popBackStack()
                    },
                )
            }
        }
    }
}
