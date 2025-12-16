package com.programmersbox.common.components

import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.MaterialExpressiveTheme
import androidx.compose.material3.MotionScheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.expressiveLightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.awt.ComposePanel
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalWindowInfo
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import javax.swing.JFrame

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

    @OptIn(ExperimentalMaterial3ExpressiveApi::class)
    actual fun authenticate(promptInfo: PromptCallback) {
        //promptInfo.onAuthenticationSucceeded()
        val window = JFrame()
        window.contentPane = ComposePanel().apply {
            setContent {
                MaterialExpressiveTheme(
                    colorScheme = if (isSystemInDarkTheme()) darkColorScheme() else expressiveLightColorScheme(),
                    motionScheme = MotionScheme.expressive(),
                ) {
                    //TODO: Need to set up proper ui
                    Surface(
                        modifier = Modifier.fillMaxSize()
                    ) {
                        Column {
                            Text(promptInfo.title)
                            Button(
                                onClick = {
                                    promptInfo.onAuthenticationSucceeded()
                                    window.isVisible = false
                                    window.dispose()
                                }
                            ) {
                                Text("Authenticate")
                            }
                        }
                    }
                }
            }
        }
        window.isAlwaysOnTop = true
        window.setSize(800, 600)
        window.isVisible = true
        window.toFront()
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
                    useSoftwareKeyboardInset = false
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