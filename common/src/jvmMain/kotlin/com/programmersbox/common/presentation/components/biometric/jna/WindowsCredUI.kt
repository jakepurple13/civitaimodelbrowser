package com.programmersbox.common.presentation.components.biometric.jna

import com.sun.jna.Library
import com.sun.jna.Native
import com.sun.jna.Pointer
import com.sun.jna.Structure
import com.sun.jna.ptr.IntByReference
import com.sun.jna.ptr.PointerByReference

/**
 * JNA bindings to credui.dll.
 * Used as fallback when WinBio hardware is absent or unenrolled.
 */
internal interface CredUI : Library {

    /**
     * Displays an OS credential prompt dialog.
     * Returns a Win32 error code (0 = success, 1223 = cancelled by user).
     */
    fun CredUIPromptForWindowsCredentialsW(
        uiInfo: CredUIInfo.ByReference?,
        authError: Int,
        authPackage: IntByReference,
        inAuthBuffer: Pointer?,
        inAuthBufferSize: Int,
        outAuthBuffer: PointerByReference,
        outBufferSize: IntByReference,
        saveCredentials: IntByReference?,
        flags: Int,
    ): Int

    companion object {
        val INSTANCE: CredUI by lazy {
            Native.load("credui", CredUI::class.java)
        }

        const val CREDUIWIN_GENERIC = 0x1
        const val ERROR_SUCCESS = 0
        const val ERROR_CANCELLED = 1223
    }
}

/**
 * CREDUI_INFO structure used to customize the CredUI dialog caption and message.
 */
@Structure.FieldOrder("cbSize", "hwndParent", "pszMessageText", "pszCaptionText", "hbmBanner")
internal open class CredUIInfo : Structure() {
    @JvmField var cbSize: Int = 0
    @JvmField var hwndParent: Pointer? = null
    @JvmField var pszMessageText: String? = null
    @JvmField var pszCaptionText: String? = null
    @JvmField var hbmBanner: Pointer? = null

    class ByReference : CredUIInfo(), Structure.ByReference
}
