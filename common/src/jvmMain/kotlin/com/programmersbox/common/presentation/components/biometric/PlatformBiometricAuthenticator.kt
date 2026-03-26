package com.programmersbox.common.presentation.components.biometric

internal sealed class BiometricResult {
    object Success : BiometricResult()
    object Failure : BiometricResult()
    data class Error(val message: String) : BiometricResult()
}

internal interface PlatformBiometricAuthenticator {
    /**
     * Blocking call — must be invoked on a background thread.
     * Returns when the OS authentication prompt completes.
     */
    fun authenticateBlocking(title: String, subtitle: String): BiometricResult
}
