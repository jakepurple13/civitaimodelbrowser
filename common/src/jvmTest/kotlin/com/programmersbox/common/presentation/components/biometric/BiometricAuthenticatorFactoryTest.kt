package com.programmersbox.common.presentation.components.biometric

import kotlin.test.Test
import kotlin.test.assertIs

class BiometricAuthenticatorFactoryTest {

    @Test
    fun `create returns MacOsBiometricAuthenticator on mac os name`() {
        val authenticator = BiometricAuthenticatorFactory.createForOs("mac os x")
        assertIs<MacOsBiometricAuthenticator>(authenticator)
    }

    @Test
    fun `create returns MacOsBiometricAuthenticator on darwin os name`() {
        val authenticator = BiometricAuthenticatorFactory.createForOs("darwin")
        assertIs<MacOsBiometricAuthenticator>(authenticator)
    }

    @Test
    fun `create returns WindowsBiometricAuthenticator on windows os name`() {
        val authenticator = BiometricAuthenticatorFactory.createForOs("windows 11")
        assertIs<WindowsBiometricAuthenticator>(authenticator)
    }

    @Test
    fun `create returns LinuxBiometricAuthenticator on linux os name`() {
        val authenticator = BiometricAuthenticatorFactory.createForOs("linux")
        assertIs<LinuxBiometricAuthenticator>(authenticator)
    }

    @Test
    fun `create returns FallbackBiometricAuthenticator on unknown os name`() {
        val authenticator = BiometricAuthenticatorFactory.createForOs("haiku")
        assertIs<FallbackBiometricAuthenticator>(authenticator)
    }

    @Test
    fun `FallbackBiometricAuthenticator always returns Success`() {
        val result = FallbackBiometricAuthenticator().authenticateBlocking("t", "s")
        assertIs<BiometricResult.Success>(result)
    }
}
