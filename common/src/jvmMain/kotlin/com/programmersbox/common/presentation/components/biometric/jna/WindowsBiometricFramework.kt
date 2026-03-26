package com.programmersbox.common.presentation.components.biometric.jna

import com.sun.jna.Callback
import com.sun.jna.Library
import com.sun.jna.Native
import com.sun.jna.Pointer
import com.sun.jna.Structure
import com.sun.jna.ptr.LongByReference

/**
 * JNA bindings to winbio.dll (Windows Biometric Framework).
 * Available on Windows 7+ with biometric hardware enrolled.
 */
internal interface WinBio : Library {

    /**
     * Opens a biometric session. Returns HRESULT (0 = S_OK).
     * @param factor WINBIO_TYPE_FINGERPRINT (0x00000008)
     * @param poolType WINBIO_POOL_SYSTEM (1)
     * @param flags WINBIO_FLAG_DEFAULT (0)
     */
    fun WinBioOpenSession(
        factor: Int,
        poolType: Int,
        flags: Int,
        unitArray: Pointer?,
        unitCount: Int,
        databaseId: Pointer?,
        sessionHandle: LongByReference,
    ): Int

    /**
     * Retrieves the identity of the currently logged-on user.
     * Must be called after WinBioOpenSession to populate the identity struct.
     */
    fun WinBioGetCurrentIdentity(
        sessionHandle: Long,
        identity: WinBioIdentity.ByReference,
    ): Int

    /**
     * Schedules an asynchronous biometric verify operation.
     * Fires [callback] when the user presents a biometric sample.
     */
    fun WinBioVerifyWithCallback(
        sessionHandle: Long,
        identity: WinBioIdentity.ByReference,
        subFactor: Int,
        callback: WinBioVerifyCallback,
        parameter: Pointer?,
    ): Int

    /**
     * Blocks the calling thread until all pending async operations complete.
     * Pair with WinBioVerifyWithCallback to turn async into blocking.
     */
    fun WinBioWait(sessionHandle: Long): Int

    fun WinBioCloseSession(sessionHandle: Long): Int

    companion object {
        val INSTANCE: WinBio by lazy {
            Native.load("winbio", WinBio::class.java)
        }

        const val WINBIO_TYPE_FINGERPRINT = 0x00000008
        const val WINBIO_POOL_SYSTEM = 1
        const val WINBIO_FLAG_DEFAULT = 0
        const val WINBIO_SUBTYPE_ANY = 0xFF

        const val S_OK = 0
        const val WINBIO_E_CANCELED = 0x80098014.toInt()
    }
}

/**
 * WINBIO_IDENTITY structure.
 * Type (Int, 4 bytes) + Value union (78 bytes) = 82 bytes total.
 * The union is the largest member: WINBIO_IDENTITY_TYPE_GUID uses a 16-byte GUID
 * plus padding. 78 bytes covers all union variants.
 */
@Structure.FieldOrder("Type", "Value")
internal open class WinBioIdentity : Structure() {
    @JvmField var Type: Int = 0
    @JvmField var Value: ByteArray = ByteArray(78)

    class ByReference : WinBioIdentity(), Structure.ByReference
}

/** Callback fired by WinBioVerifyWithCallback when verification completes. */
internal interface WinBioVerifyCallback : Callback {
    fun invoke(
        parameter: Pointer?,
        operationStatus: Int,   // HRESULT
        unitId: Int,
        match: Boolean,
        rejectDetail: Int,
    )
}
