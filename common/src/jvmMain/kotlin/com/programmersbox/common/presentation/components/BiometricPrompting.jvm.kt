package com.programmersbox.common.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalWindowInfo
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.programmersbox.common.presentation.components.biometric.BiometricAuthenticatorFactory
import com.programmersbox.common.presentation.components.biometric.BiometricResult
import com.programmersbox.common.presentation.components.biometric.PlatformBiometricAuthenticator
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

actual class BiometricPrompting internal constructor(
    private val useStrongSecurity: Boolean = false,
    private val useDeviceCredentials: Boolean = true,
    private val onAuthenticationSucceeded: () -> Unit = {},
    private val onAuthenticationFailed: () -> Unit = {},
    internal val authenticator: PlatformBiometricAuthenticator = BiometricAuthenticatorFactory.create(),
    internal val ioDispatcher: CoroutineDispatcher = Dispatchers.IO,
) {
    constructor(
        useStrongSecurity: Boolean = false,
        useDeviceCredentials: Boolean = true,
        onAuthenticationSucceeded: () -> Unit = {},
        onAuthenticationFailed: () -> Unit = {},
    ) : this(
        useStrongSecurity = useStrongSecurity,
        useDeviceCredentials = useDeviceCredentials,
        onAuthenticationSucceeded = onAuthenticationSucceeded,
        onAuthenticationFailed = onAuthenticationFailed,
        authenticator = BiometricAuthenticatorFactory.create(),
        ioDispatcher = Dispatchers.IO,
    )

    private val scope = CoroutineScope(ioDispatcher + SupervisorJob())

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
            negativeButtonText = negativeButtonText,
        )
    )

    actual fun authenticate(promptInfo: PromptCallback) {
        scope.launch {
            val result = authenticator.authenticateBlocking(
                title = promptInfo.title,
                subtitle = promptInfo.subtitle,
            )
            withContext(Dispatchers.Main) {
                when (result) {
                    is BiometricResult.Success -> promptInfo.onAuthenticationSucceeded()
                    is BiometricResult.Failure -> promptInfo.onAuthenticationFailed()
                    is BiometricResult.Error -> promptInfo.onAuthenticationFailed()
                }
            }
        }
    }
}

@Composable
actual fun rememberBiometricPrompting(
    title: String,
    onAuthenticationSucceeded: () -> Unit,
    onAuthenticationFailed: () -> Unit,
): BiometricPrompting = remember {
    BiometricPrompting(
        useStrongSecurity = false,
        useDeviceCredentials = true,
        onAuthenticationSucceeded = onAuthenticationSucceeded,
        onAuthenticationFailed = onAuthenticationFailed,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
actual fun HideScreen(shouldHide: Boolean) {
    if (shouldHide) {
        val window = LocalWindowInfo.current
        if (!window.isWindowFocused) {
            Dialog(
                onDismissRequest = {},
                properties = DialogProperties(
                    usePlatformDefaultWidth = false,
                    dismissOnBackPress = false,
                    dismissOnClickOutside = false,
                    usePlatformInsets = false,
                    useSoftwareKeyboardInset = false,
                )
            ) {
                Box(
                    modifier = Modifier
                        .background(Color.Black)
                        .fillMaxSize()
                )
            }
        }
    }
}
