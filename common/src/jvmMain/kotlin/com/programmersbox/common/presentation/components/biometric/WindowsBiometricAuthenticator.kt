package com.programmersbox.common.presentation.components.biometric

import com.programmersbox.common.presentation.components.biometric.jna.CredUI
import com.programmersbox.common.presentation.components.biometric.jna.CredUIInfo
import com.programmersbox.common.presentation.components.biometric.jna.WinBio
import com.programmersbox.common.presentation.components.biometric.jna.WinBioIdentity
import com.programmersbox.common.presentation.components.biometric.jna.WinBioVerifyCallback
import com.sun.jna.Pointer
import com.sun.jna.ptr.IntByReference
import com.sun.jna.ptr.LongByReference
import com.sun.jna.ptr.PointerByReference

internal class WindowsBiometricAuthenticator : PlatformBiometricAuthenticator {

    override fun authenticateBlocking(title: String, subtitle: String): BiometricResult {
        return try {
            authenticateWithWinBio(title, subtitle)
        } catch (_: UnsatisfiedLinkError) {
            // winbio.dll absent on Windows N/KN editions
            authenticateWithCredUI(title, subtitle)
        } catch (e: Exception) {
            BiometricResult.Error(e.message ?: "Windows biometric error")
        }
    }

    private fun authenticateWithWinBio(title: String, subtitle: String): BiometricResult {
        val winBio = WinBio.INSTANCE
        val sessionHandle = LongByReference()

        val openHr = winBio.WinBioOpenSession(
            WinBio.WINBIO_TYPE_FINGERPRINT,
            WinBio.WINBIO_POOL_SYSTEM,
            WinBio.WINBIO_FLAG_DEFAULT,
            null, 0, null,
            sessionHandle
        )
        if (openHr != WinBio.S_OK) {
            // No biometric hardware or no enrolled samples — fall back to CredUI
            return authenticateWithCredUI(title, subtitle)
        }

        val identity = WinBioIdentity.ByReference()
        val identityHr = winBio.WinBioGetCurrentIdentity(sessionHandle.value, identity)
        if (identityHr != WinBio.S_OK) {
            winBio.WinBioCloseSession(sessionHandle.value)
            return authenticateWithCredUI(title, subtitle)
        }

        var result: BiometricResult = BiometricResult.Failure
        val callback = object : WinBioVerifyCallback {
            override fun invoke(
                parameter: Pointer?,
                operationStatus: Int,
                unitId: Int,
                match: Boolean,
                rejectDetail: Int,
            ) {
                result = when {
                    operationStatus == WinBio.S_OK && match -> BiometricResult.Success
                    operationStatus == WinBio.WINBIO_E_CANCELED -> BiometricResult.Failure
                    else -> BiometricResult.Failure
                }
            }
        }

        try {
            winBio.WinBioVerifyWithCallback(
                sessionHandle.value, identity,
                WinBio.WINBIO_SUBTYPE_ANY, callback, null
            )
            // WinBioWait blocks until the async callback fires — no latch needed.
            winBio.WinBioWait(sessionHandle.value)
        } finally {
            winBio.WinBioCloseSession(sessionHandle.value)
        }

        return result
    }

    private fun authenticateWithCredUI(title: String, subtitle: String): BiometricResult {
        return try {
            val credUI = CredUI.INSTANCE
            val uiInfo = CredUIInfo.ByReference().apply {
                pszCaptionText = title
                pszMessageText = subtitle
                cbSize = size()
            }
            val authPackage = IntByReference(0)
            val outBuffer = PointerByReference()
            val outBufferSize = IntByReference(0)

            val result = credUI.CredUIPromptForWindowsCredentialsW(
                uiInfo, 0, authPackage,
                null, 0, outBuffer, outBufferSize, null,
                CredUI.CREDUIWIN_GENERIC
            )
            // Free the output buffer allocated by CredUI regardless of outcome
            try {
                val bufPtr = outBuffer.value
                if (bufPtr != null) com.sun.jna.Native.free(com.sun.jna.Pointer.nativeValue(bufPtr))
            } catch (_: Exception) { /* best-effort cleanup */ }

            when (result) {
                CredUI.ERROR_SUCCESS -> BiometricResult.Success
                CredUI.ERROR_CANCELLED -> BiometricResult.Failure
                else -> BiometricResult.Error("CredUI returned error code $result")
            }
        } catch (e: Exception) {
            BiometricResult.Error("CredUI unavailable: ${e.message}")
        }
    }
}
