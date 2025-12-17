package com.programmersbox.common.components

import android.content.Context
import android.content.ContextWrapper
import android.os.Build
import android.view.WindowManager
import android.widget.Toast
import androidx.activity.compose.LocalActivity
import androidx.biometric.AuthenticationRequest
import androidx.biometric.BiometricManager
import androidx.biometric.compose.rememberAuthenticationLauncher
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.fragment.app.FragmentActivity

actual class BiometricPrompting(
    private val context: Context,
    private val useStrongSecurity: Boolean,
    private val useDeviceCredentials: Boolean,
    private val onLaunch: () -> Unit,
) {
    val biometricManager by lazy { BiometricManager.from(context) }

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
        val biometricStrength = if (useStrongSecurity)
            BiometricManager.Authenticators.BIOMETRIC_STRONG
        else
            BiometricManager.Authenticators.BIOMETRIC_WEAK

        if (biometricManager.canAuthenticate(biometricStrength) == BiometricManager.BIOMETRIC_ERROR_NONE_ENROLLED) {
            promptInfo.onAuthenticationSucceeded()
            return
        }

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

    val bio = rememberAuthenticationLauncher {
        if (it.isSuccess()) {
            onAuthenticationSucceeded()
        } else if (it.isError()) {
            println(it.error()?.errString)
            if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.S_V2)
                Toast.makeText(context, it.error()?.errString, Toast.LENGTH_LONG).show()

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
                        authFallback = AuthenticationRequest.Biometric.Fallback.DeviceCredential
                    ) {
                        setContent(
                            AuthenticationRequest.BodyContent.PlainText(
                                description = "Authentication is required to view this content."
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

private tailrec fun Context.findActivity(): FragmentActivity = when (this) {
    is FragmentActivity -> this
    is ContextWrapper -> this.baseContext.findActivity()
    else -> error("Could not find activity in Context chain.")
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