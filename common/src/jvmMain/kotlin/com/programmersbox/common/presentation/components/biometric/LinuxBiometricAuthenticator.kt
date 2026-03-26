package com.programmersbox.common.presentation.components.biometric

import com.programmersbox.common.presentation.components.biometric.jna.LibPam
import com.programmersbox.common.presentation.components.biometric.jna.PamConv
import com.programmersbox.common.presentation.components.biometric.jna.PamConvCallback
import com.sun.jna.Native
import com.sun.jna.Pointer
import com.sun.jna.ptr.PointerByReference
import java.util.concurrent.TimeUnit

internal class LinuxBiometricAuthenticator : PlatformBiometricAuthenticator {

    override fun authenticateBlocking(title: String, subtitle: String): BiometricResult {
        return try {
            // Tier 1: pkexec — polkit native dialog (supports fprintd on most distros)
            val pkexecResult = pkexecAuthenticate()
            if (pkexecResult != null) return pkexecResult

            // Tier 2: direct libpam
            pamAuthenticate()
        } catch (e: Exception) {
            BiometricResult.Error(e.message ?: "Linux auth error")
        }
    }

    /**
     * Runs `pkexec --disable-internal-agent true` which shows the polkit authentication dialog.
     * On distros with fprintd configured, the dialog offers fingerprint authentication.
     * Returns null when pkexec is not found or exits unexpectedly, so the caller can fall through.
     *
     * Exit codes:
     *   0   → user authenticated successfully
     *   126 → user dismissed / not authorized
     *   127 → pkexec not found in PATH
     */
    private fun pkexecAuthenticate(): BiometricResult? {
        return try {
            val process = ProcessBuilder("pkexec", "--disable-internal-agent", "true")
                .redirectErrorStream(true)
                .start()
            val finished = process.waitFor(30, TimeUnit.SECONDS)
            if (!finished) {
                process.destroyForcibly()
                return null  // timeout — fall through to PAM
            }
            when (process.exitValue()) {
                0 -> BiometricResult.Success
                126 -> BiometricResult.Failure  // dismissed or not authorized
                127 -> null                     // pkexec not found — fall through to PAM
                else -> null                    // unexpected — fall through to PAM
            }
        } catch (_: Exception) {
            null  // pkexec not available
        }
    }

    /**
     * Authenticates via libpam using the "login" service.
     * For fprintd-enabled PAM stacks (e.g. /etc/pam.d/login includes pam_fprintd.so),
     * the conversation callback replies with empty strings and fprintd handles the sensor.
     *
     * IMPORTANT: pam_response arrays and their resp strings MUST be allocated with
     * Native.malloc (C heap), not JNA Memory (JVM heap). The PAM library calls free()
     * on them after the transaction — free() on JVM heap is undefined behaviour.
     */
    private fun pamAuthenticate(): BiometricResult {
        val pam = try {
            LibPam.INSTANCE
        } catch (_: UnsatisfiedLinkError) {
            return BiometricResult.Error("libpam not available on this system")
        }

        val username = System.getProperty("user.name")
            ?: return BiometricResult.Error("Cannot determine current user")

        val pamh = PointerByReference()
        val pointerSize = Native.POINTER_SIZE.toLong()
        val responseEntrySize = pointerSize + 4L  // char* + int

        val conv = object : PamConvCallback {
            override fun invoke(
                numMsg: Int,
                msg: Pointer,
                resp: PointerByReference,
                appdata_ptr: Pointer?,
            ): Int {
                // Allocate on the C heap so PAM can free() it
                val responseArrayPtr = Native.malloc(numMsg * responseEntrySize)
                val responseArray = Pointer(responseArrayPtr)
                for (i in 0 until numMsg) {
                    // Read msg_style from pam_message: first 4 bytes
                    val msgPtr = msg.getPointer(i * pointerSize)
                    val msgStyle = msgPtr.getInt(0)

                    val offset = i * responseEntrySize
                    when (msgStyle) {
                        LibPam.PAM_PROMPT_ECHO_OFF, LibPam.PAM_PROMPT_ECHO_ON -> {
                            // Allocate null-terminated empty string on C heap (PAM will free it)
                            val emptyStrPtr = Native.malloc(1)
                            Pointer(emptyStrPtr).setByte(0, 0)
                            responseArray.setPointer(offset, Pointer(emptyStrPtr))
                        }
                        else -> responseArray.setPointer(offset, null)
                    }
                    responseArray.setInt(offset + pointerSize, 0)  // resp_retcode = 0
                }
                resp.value = responseArray
                return LibPam.PAM_SUCCESS
            }
        }

        val pamConv = PamConv.ByReference().apply { this.conv = conv }
        val startResult = pam.pam_start("login", username, pamConv, pamh)
        if (startResult != LibPam.PAM_SUCCESS) {
            return BiometricResult.Error("pam_start failed with code $startResult")
        }

        val authResult = pam.pam_authenticate(pamh.value, 0)
        pam.pam_end(pamh.value, authResult)

        java.lang.ref.Reference.reachabilityFence(conv)
        java.lang.ref.Reference.reachabilityFence(pamConv)

        return when (authResult) {
            LibPam.PAM_SUCCESS -> BiometricResult.Success
            else -> BiometricResult.Failure
        }
    }
}
