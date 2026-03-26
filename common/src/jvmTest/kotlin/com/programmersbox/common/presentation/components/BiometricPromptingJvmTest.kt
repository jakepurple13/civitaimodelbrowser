package com.programmersbox.common.presentation.components

import com.programmersbox.common.presentation.components.biometric.BiometricResult
import com.programmersbox.common.presentation.components.biometric.PlatformBiometricAuthenticator
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertTrue

@OptIn(ExperimentalCoroutinesApi::class)
class BiometricPromptingJvmTest {

    private val testDispatcher = StandardTestDispatcher()

    @BeforeTest
    fun setup() {
        Dispatchers.setMain(testDispatcher)
    }

    @AfterTest
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `authenticate calls onAuthenticationSucceeded on Success`() = runTest {
        var succeeded = false
        val prompting = BiometricPrompting(
            authenticator = FakeAuthenticator(BiometricResult.Success),
            ioDispatcher = testDispatcher,
        )
        prompting.authenticate(
            onAuthenticationSucceeded = { succeeded = true },
            onAuthenticationFailed = { },
        )
        advanceUntilIdle()
        assertTrue(succeeded, "Expected onAuthenticationSucceeded to be called")
    }

    @Test
    fun `authenticate calls onAuthenticationFailed on Failure`() = runTest {
        var failed = false
        val prompting = BiometricPrompting(
            authenticator = FakeAuthenticator(BiometricResult.Failure),
            ioDispatcher = testDispatcher,
        )
        prompting.authenticate(
            onAuthenticationSucceeded = { },
            onAuthenticationFailed = { failed = true },
        )
        advanceUntilIdle()
        assertTrue(failed, "Expected onAuthenticationFailed to be called on Failure")
    }

    @Test
    fun `authenticate calls onAuthenticationFailed on Error`() = runTest {
        var failed = false
        val prompting = BiometricPrompting(
            authenticator = FakeAuthenticator(BiometricResult.Error("native error")),
            ioDispatcher = testDispatcher,
        )
        prompting.authenticate(
            onAuthenticationSucceeded = { },
            onAuthenticationFailed = { failed = true },
        )
        advanceUntilIdle()
        assertTrue(failed, "Expected onAuthenticationFailed to be called on Error")
    }
}

private class FakeAuthenticator(private val result: BiometricResult) : PlatformBiometricAuthenticator {
    override fun authenticateBlocking(title: String, subtitle: String): BiometricResult = result
}
