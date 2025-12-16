package com.programmersbox.common.components

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
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
expect fun rememberBiometricPrompting(): BiometricPrompting

@Composable
fun rememberBiometricOpening(): BiometricOpen {
    val biometricPrompting = rememberBiometricPrompting()
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
        openAction: () -> Unit,
        customListItem: CustomListItem
    ) {
        biometricPrompting.authenticate(
            onAuthenticationSucceeded = openAction,
            title = "Authenticate to view ${customListItem.name}",
            subtitle = "Authenticate to view media",
            negativeButtonText = "Cancel"
        )
    }
}

@Composable
expect fun HideScreen(shouldHide: Boolean)