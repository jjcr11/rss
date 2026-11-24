package com.jjcr11.rss.presentation.settingsscreen

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.jjcr11.rss.presentation.components.ArrowBackIcon
import com.jjcr11.rss.presentation.components.RssButton
import com.jjcr11.rss.presentation.theme.RssTheme
import com.jjcr11.rss.resources.Res
import com.jjcr11.rss.resources.logout_button
import com.jjcr11.rss.resources.navigate_back
import com.jjcr11.rss.resources.settings_title
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun SettingsRoute(
    modifier: Modifier = Modifier,
    viewModel: SettingsViewModel = koinViewModel(),
    onBackClick: () -> Unit = {},
    onLogoutSuccess: () -> Unit = {},
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(
        key1 = uiState.isLoggedOut,
        block = {
            if (uiState.isLoggedOut) {
                onLogoutSuccess()
            }
        },
    )

    SettingsScreen(
        modifier = modifier,
        uiState = uiState,
        onBackClick = onBackClick,
        onLogoutClick = {
            viewModel.onEvent(event = SettingsUiEvent.Logout)
        },
    )
}

@Composable
fun SettingsScreen(
    modifier: Modifier = Modifier,
    uiState: SettingsUiState = SettingsUiState(),
    onBackClick: () -> Unit = {},
    onLogoutClick: () -> Unit = {},
) {
    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = {
                    Text(text = stringResource(resource = Res.string.settings_title))
                },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            imageVector = ArrowBackIcon,
                            contentDescription = stringResource(resource = Res.string.navigate_back),
                        )
                    }
                },
            )
        },
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues = innerPadding)
                .padding(all = 16.dp),
        ) {
            Column(
                modifier = Modifier.fillMaxSize(),
            ) {
                if (uiState.errorMessage != null) {
                    Text(
                        text = uiState.errorMessage,
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodyMedium,
                    )
                    Spacer(modifier = Modifier.height(height = 16.dp))
                }

                RssButton(
                    text = stringResource(resource = Res.string.logout_button),
                    onClick = onLogoutClick,
                    isLoading = uiState.isLoading,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.error,
                        contentColor = MaterialTheme.colorScheme.onError,
                    ),
                )
            }
        }
    }
}

@Composable
@Preview
fun SettingsScreenPreview() {
    RssTheme {
        SettingsScreen()
    }
}

@Composable
@Preview
fun SettingsScreenDarkPreview() {
    RssTheme(
        darkTheme = true,
        content = {
            SettingsScreen()
        },
    )
}
