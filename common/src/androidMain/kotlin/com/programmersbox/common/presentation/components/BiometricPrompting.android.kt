package com.programmersbox.common.presentation.components

import android.content.Context
import android.content.Intent
import android.os.Build
import android.provider.Settings
import android.view.WindowManager
import android.widget.Toast
import androidx.activity.compose.LocalActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.biometric.AuthenticationRequest
import androidx.biometric.BiometricManager
import androidx.biometric.PromptContentItemPlainText
import androidx.biometric.compose.rememberAuthenticationLauncher
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext

actual class BiometricPrompting(
    private val context: Context,
    private val useStrongSecurity: Boolean,
    private val useDeviceCredentials: Boolean,
    private val onLaunch: () -> Unit,
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
        onLaunch()
    }
}

@Composable
actual fun rememberBiometricPrompting(
    title: String,
    onAuthenticationSucceeded: () -> Unit,
    onAuthenticationFailed: () -> Unit,
): BiometricPrompting {
    val context = LocalContext.current
    var isSetup by remember { mutableStateOf(false) }

    val launcher = rememberLauncherForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) {

    }

    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
        if (isSetup) {
            AskForBiometricSetup(
                onDismiss = { isSetup = false },
                onSetup = {
                    launcher.launch(
                        Intent(Settings.ACTION_BIOMETRIC_ENROLL).apply {
                            putExtra(
                                Settings.EXTRA_BIOMETRIC_AUTHENTICATORS_ALLOWED,
                                BiometricManager.Authenticators.BIOMETRIC_STRONG or
                                        BiometricManager.Authenticators.DEVICE_CREDENTIAL
                            )
                        }
                    )
                    isSetup = false
                }
            )
        }
    }

    val bio = rememberAuthenticationLauncher {
        if (it.isSuccess()) {
            onAuthenticationSucceeded()
        } else if (it.isError()) {
            println("${it.error()?.errorCode}, ${it.error()?.errString}")
            if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.S_V2)
                Toast.makeText(context, it.error()?.errString, Toast.LENGTH_LONG).show()

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                isSetup = true
            }

            onAuthenticationFailed()
        }
    }

    val biometricPrompt = remember(context) {
        BiometricPrompting(
            context = context,
            useStrongSecurity = false,
            useDeviceCredentials = true,
            onLaunch = {
                bio.launch(
                    AuthenticationRequest.biometricRequest(
                        title = title,
                        AuthenticationRequest.Biometric.Fallback.DeviceCredential
                    ) {
                        setContent(
                            AuthenticationRequest.BodyContent.VerticalList(
                                description = "Authentication is required to view this content.",
                                items = listOf(
                                    PromptContentItemPlainText("The user does not want anyone to see this!"),
                                )
                            )
                        )
                        setMinStrength(AuthenticationRequest.Biometric.Strength.Class2)
                    }
                )
            }
        )
    }

    return biometricPrompt
}

@Composable
private fun AskForBiometricSetup(
    onDismiss: () -> Unit,
    onSetup: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Biometric Setup Required") },
        text = { Text("Please set up your biometric credentials.") },
        confirmButton = {
            TextButton(
                onClick = onSetup,
            ) { Text("Take me to Setup") }
        },
        dismissButton = {
            TextButton(
                onClick = onDismiss,
            ) { Text("Cancel") }
        }
    )
}

@Composable
actual fun HideScreen(shouldHide: Boolean) {
    val window = LocalActivity.current

    DisposableEffect(shouldHide) {
        if (shouldHide) {
            window?.window?.addFlags(WindowManager.LayoutParams.FLAG_SECURE)
        }
        onDispose { window?.window?.clearFlags(WindowManager.LayoutParams.FLAG_SECURE) }
    }
}