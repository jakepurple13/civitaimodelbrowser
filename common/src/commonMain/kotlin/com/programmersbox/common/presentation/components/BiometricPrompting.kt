package com.programmersbox.common.presentation.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalWindowInfo
import com.programmersbox.common.BackButton
import com.programmersbox.common.db.CustomListItem

expect class BiometricPrompting {

    fun authenticate(
        onAuthenticationSucceeded: () -> Unit,
        onAuthenticationFailed: () -> Unit = {},
        title: String = "Authentication required",
        subtitle: String = "Please Authenticate",
        negativeButtonText: String = "Never Mind",
    )

    fun authenticate(promptInfo: PromptCallback)
}

data class PromptCallback(
    val onAuthenticationSucceeded: () -> Unit,
    val onAuthenticationFailed: () -> Unit = {},
    val title: String = "Authentication required",
    val subtitle: String = "Please Authenticate",
    val negativeButtonText: String = "Never Mind",
)

@Composable
fun rememberBiometricPrompt(
    onAuthenticationSucceeded: () -> Unit,
    onAuthenticationFailed: () -> Unit = {},
    title: String = "Authentication required",
    subtitle: String = "Please Authenticate",
    negativeButtonText: String = "Never Mind",
): PromptCallback {
    val succeed by rememberUpdatedState(onAuthenticationSucceeded)
    val failed by rememberUpdatedState(onAuthenticationFailed)
    return remember(succeed, failed, title, subtitle, negativeButtonText) {
        PromptCallback(
            onAuthenticationSucceeded = succeed,
            onAuthenticationFailed = failed,
            title = title,
            subtitle = subtitle,
            negativeButtonText = negativeButtonText
        )
    }
}

@Composable
expect fun rememberBiometricPrompting(
    title: String,
    onAuthenticationSucceeded: () -> Unit = {},
    onAuthenticationFailed: () -> Unit = {},
): BiometricPrompting

@Composable
fun rememberBiometricOpening(
    title: String,
    onAuthenticationSucceeded: () -> Unit = {},
    onAuthenticationFailed: () -> Unit = {},
): BiometricOpen {
    val biometricPrompting = rememberBiometricPrompting(
        title = title,
        onAuthenticationSucceeded = onAuthenticationSucceeded,
        onAuthenticationFailed = onAuthenticationFailed,
    )
    return remember(biometricPrompting) {
        BiometricOpen(
            biometricPrompting = biometricPrompting,
        )
    }
}

class BiometricOpen(
    private val biometricPrompting: BiometricPrompting,
) {
    fun authenticate(
        customListItem: CustomListItem
    ) {
        biometricPrompting.authenticate(
            onAuthenticationSucceeded = {},
            title = "Authenticate to view ${customListItem.name}",
            subtitle = "Authenticate to view media",
            negativeButtonText = "Cancel"
        )
    }
}

@Composable
expect fun HideScreen(shouldHide: Boolean)

@Composable
fun SecureScreenWrapper(
    shouldSecure: Boolean,
    content: @Composable () -> Unit,
) {
    HideScreen(shouldHide = shouldSecure)

    val windowInfo = LocalWindowInfo.current.isWindowFocused

    Box(modifier = Modifier.fillMaxSize()) {
        // Draw the actual screen content
        content()

        // Draw the privacy overlay if the app loses focus
        AnimatedVisibility(
            !windowInfo && shouldSecure,
            enter = fadeIn(),
            exit = fadeOut()
        ) {
            Scaffold(
                topBar = {
                    CenterAlignedTopAppBar(
                        title = { Text("Nice Try!") },
                        navigationIcon = { BackButton() }
                    )
                },
            ) { padding ->
                Box(
                    modifier = Modifier
                        .padding(padding)
                        .fillMaxSize()
                        .background(MaterialTheme.colorScheme.surface)
                )
            }
        }
    }
}