package com.programmersbox.common.components

import android.content.Context
import android.content.ContextWrapper
import android.view.WindowManager
import androidx.activity.compose.LocalActivity
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricPrompt
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.fragment.app.FragmentActivity

actual class BiometricPrompting(
    private val context: Context,
    private val useStrongSecurity: Boolean,
    private val useDeviceCredentials: Boolean,
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
        var biometricStrength = if (useStrongSecurity)
            BiometricManager.Authenticators.BIOMETRIC_STRONG
        else
            BiometricManager.Authenticators.BIOMETRIC_WEAK

        if (useDeviceCredentials) biometricStrength =
            biometricStrength or BiometricManager.Authenticators.DEVICE_CREDENTIAL

        if (biometricManager.canAuthenticate(biometricStrength) == BiometricManager.BIOMETRIC_ERROR_NONE_ENROLLED) {
            promptInfo.onAuthenticationSucceeded()
            return
        }

        BiometricPrompt(
            context.findActivity(),
            context.mainExecutor,
            object : BiometricPrompt.AuthenticationCallback() {
                override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                    super.onAuthenticationError(errorCode, errString)
                    promptInfo.onAuthenticationFailed()
                }

                override fun onAuthenticationSucceeded(
                    result: BiometricPrompt.AuthenticationResult,
                ) {
                    super.onAuthenticationSucceeded(result)
                    promptInfo.onAuthenticationSucceeded()
                }

                override fun onAuthenticationFailed() {
                    super.onAuthenticationFailed()
                    promptInfo.onAuthenticationFailed()
                }
            }
        ).authenticate(
            BiometricPrompt.PromptInfo.Builder()
                .setTitle(promptInfo.title)
                .setSubtitle(promptInfo.subtitle)
                .also {
                    if (!useDeviceCredentials) {
                        it.setNegativeButtonText(promptInfo.negativeButtonText)
                    }
                }
                .setAllowedAuthenticators(biometricStrength)
                .build()
        )
    }
}

@Composable
actual fun rememberBiometricPrompting(): BiometricPrompting {
    val context = LocalContext.current

    val biometricPrompt = remember(context) {
        BiometricPrompting(
            context = context,
            useStrongSecurity = false,
            useDeviceCredentials = true,
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