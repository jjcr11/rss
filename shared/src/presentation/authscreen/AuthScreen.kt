package com.jjcr11.rss.presentation.authscreen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.jjcr11.rss.presentation.components.RssButton
import com.jjcr11.rss.presentation.components.RssFeedIcon
import com.jjcr11.rss.presentation.components.RssTextField
import com.jjcr11.rss.presentation.theme.RssTheme
import com.jjcr11.rss.resources.Res
import com.jjcr11.rss.resources.continue_without_account_button
import com.jjcr11.rss.resources.email_placeholder
import com.jjcr11.rss.resources.password_placeholder
import com.jjcr11.rss.resources.sign_up_button
import com.jjcr11.rss.resources.sign_up_with_google_button
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun AuthRoute(
    modifier: Modifier = Modifier,
    viewModel: AuthViewModel = koinViewModel(),
    onGoogleSignUpClick: () -> Unit = {},
    onContinueWithoutAccountClick: () -> Unit = {},
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    AuthScreen(
        modifier = modifier,
        uiState = uiState,
        onSignUpClick = { email, password ->
            viewModel.onEvent(
                event = AuthUiEvent.SignUpWithEmail(
                    email = email,
                    password = password,
                ),
            )
        },
        onGoogleSignUpClick = onGoogleSignUpClick,
        onContinueWithoutAccountClick = onContinueWithoutAccountClick,
    )
}

@Composable
fun AuthScreen(
    modifier: Modifier = Modifier,
    uiState: AuthUiState = AuthUiState(),
    onSignUpClick: (email: String, password: String) -> Unit = { _, _ -> },
    onGoogleSignUpClick: () -> Unit = {},
    onContinueWithoutAccountClick: () -> Unit = {},
) {
    var email by remember { mutableStateOf(value = "") }
    var password by remember { mutableStateOf(value = "") }

    Scaffold(
        modifier = modifier,
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues = innerPadding)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.Top,
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Spacer(modifier = Modifier.height(height = 48.dp))

            Icon(
                imageVector = RssFeedIcon,
                contentDescription = null,
                modifier = Modifier.size(size = 80.dp),
                tint = MaterialTheme.colorScheme.primary,
            )

            Spacer(modifier = Modifier.height(height = 32.dp))

            RssTextField(
                value = email,
                onValueChange = { newEmail ->
                    email = newEmail
                },
                placeholder = {
                    Text(
                        text = stringResource(resource = Res.string.email_placeholder),
                        style = MaterialTheme.typography.labelMedium,
                    )
                },
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Email,
                    imeAction = ImeAction.Next,
                ),
            )

            Spacer(modifier = Modifier.height(height = 16.dp))

            RssTextField(
                value = password,
                onValueChange = { newPassword ->
                    password = newPassword
                },
                placeholder = {
                    Text(
                        text = stringResource(resource = Res.string.password_placeholder),
                        style = MaterialTheme.typography.labelMedium,
                    )
                },
                visualTransformation = PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Password,
                    imeAction = ImeAction.Done,
                ),
            )

            uiState.errorMessage?.let { message ->
                Spacer(modifier = Modifier.height(height = 8.dp))
                Text(
                    text = message,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.labelSmall,
                )
            }

            Spacer(modifier = Modifier.height(height = 24.dp))

            RssButton(
                text = stringResource(resource = Res.string.sign_up_button),
                onClick = {
                    onSignUpClick(email, password)
                },
                isLoading = uiState.isLoading,
            )

            Spacer(modifier = Modifier.height(height = 16.dp))

            HorizontalDivider(
                color = MaterialTheme.colorScheme.outlineVariant,
                thickness = 1.dp,
            )

            Spacer(modifier = Modifier.height(height = 16.dp))

            RssButton(
                text = stringResource(resource = Res.string.sign_up_with_google_button),
                onClick = onGoogleSignUpClick,
                enabled = !uiState.isLoading,
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.secondaryContainer,
                    contentColor = MaterialTheme.colorScheme.onSecondaryContainer,
                ),
            )

            Spacer(modifier = Modifier.height(height = 8.dp))

            TextButton(
                onClick = onContinueWithoutAccountClick,
                enabled = !uiState.isLoading,
            ) {
                Text(
                    text = stringResource(resource = Res.string.continue_without_account_button),
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.primary,
                )
            }
        }
    }
}

@Composable
@Preview
fun AuthScreenPreview() {
    RssTheme {
        AuthScreen()
    }
}

@Composable
@Preview
fun AuthScreenDarkPreview() {
    RssTheme(
        darkTheme = true,
        content = {
            AuthScreen()
        },
    )
}
