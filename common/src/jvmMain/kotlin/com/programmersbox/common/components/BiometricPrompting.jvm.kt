package com.programmersbox.common.components

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember

//TODO: Need to implement
actual class BiometricPrompting(
    private val useStrongSecurity: Boolean,
    private val useDeviceCredentials: Boolean,
) {
    actual fun authenticate(
        onAuthenticationSucceeded: () -> Unit,
        onAuthenticationFailed: () -> Unit,
        title: String,
        subtitle: String,
        negativeButtonText: String,
    ) = authenticate(
        PromptCallback(
            onAuthenticationSucceeded = onAuthenticationSucceeded,
            onAuthenticationFailed = onAuthenticationFailed,
            title = title,
            subtitle = subtitle,
            negativeButtonText = negativeButtonText
        )
    )

    actual fun authenticate(promptInfo: PromptCallback) {
        promptInfo.onAuthenticationSucceeded()
    }
}

@Composable
actual fun rememberBiometricPrompting(): BiometricPrompting {
    val biometricPrompt = remember {
        BiometricPrompting(
            useStrongSecurity = false,
            useDeviceCredentials = true,
        )
    }

    return biometricPrompt
}

@Composable
actual fun HideScreen(shouldHide: Boolean) {

}