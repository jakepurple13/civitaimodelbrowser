package com.programmersbox.common.presentation.components.biometric

internal object BiometricAuthenticatorFactory {

    fun create(): PlatformBiometricAuthenticator = createForOs(System.getProperty("os.name"))

    internal fun createForOs(osName: String): PlatformBiometricAuthenticator {
        val os = osName.lowercase()
        return when {
            os.contains("mac") || os.contains("darwin") -> MacOsBiometricAuthenticator()
            os.contains("win") -> WindowsBiometricAuthenticator()
            os.contains("nux") || os.contains("nix") || os.contains("aix") -> LinuxBiometricAuthenticator()
            else -> FallbackBiometricAuthenticator()
        }
    }
}

internal class FallbackBiometricAuthenticator : PlatformBiometricAuthenticator {
    override fun authenticateBlocking(title: String, subtitle: String): BiometricResult =
        BiometricResult.Error("Unsupported OS: ${System.getProperty("os.name")}")
}

