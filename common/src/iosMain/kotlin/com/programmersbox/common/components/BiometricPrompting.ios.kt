package com.programmersbox.common.components

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import com.github.zaval.biometricauthentificator.BiometricAuthHelper
import com.github.zaval.biometricauthentificator.rememberBiometricAuthHelper

actual class BiometricPrompting(
    private val biometricAuthHelper: BiometricAuthHelper
) {
    actual fun authenticate(
        onAuthenticationSucceeded: () -> Unit,
        onAuthenticationFailed: () -> Unit,
        title: String,
        subtitle: String,
        negativeButtonText: String
    ) {
        authenticate(
            PromptCallback(
                onAuthenticationSucceeded = onAuthenticationSucceeded,
                onAuthenticationFailed = onAuthenticationFailed,
                title = title,
                subtitle = subtitle,
                negativeButtonText = negativeButtonText
            )
        )
    }

    actual fun authenticate(promptInfo: PromptCallback) {
        biometricAuthHelper.authenticate(
            onFailure = { promptInfo.onAuthenticationFailed() },
            onSuccess = { promptInfo.onAuthenticationSucceeded() }
        )
    }
}

@Composable
actual fun rememberBiometricPrompting(
    title: String,
    onAuthenticationSucceeded: () -> Unit,
    onAuthenticationFailed: () -> Unit
): BiometricPrompting {
    val d = rememberBiometricAuthHelper(
        title = title,
        subTitle = "Please Authenticate",
        cancelText = "Never Mind",
    )
    return remember(d) { BiometricPrompting(d) }
}

@Composable
actual fun HideScreen(shouldHide: Boolean) {
}