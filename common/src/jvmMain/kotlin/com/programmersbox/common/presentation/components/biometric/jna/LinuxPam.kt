package com.programmersbox.common.presentation.components.biometric.jna

import com.sun.jna.Callback
import com.sun.jna.Library
import com.sun.jna.Native
import com.sun.jna.Pointer
import com.sun.jna.Structure
import com.sun.jna.ptr.PointerByReference

/**
 * JNA bindings to libpam (Linux-PAM).
 * Available on all major Linux distributions.
 */
internal interface LibPam : Library {

    /**
     * Initialises a PAM transaction.
     * @param serviceName PAM service name — reads /etc/pam.d/<serviceName>. Use "login".
     * @param user OS username (System.getProperty("user.name"))
     * @param pamConv conversation callback structure
     * @param pamh out — opaque PAM handle
     */
    fun pam_start(
        serviceName: String,
        user: String,
        pamConv: PamConv.ByReference,
        pamh: PointerByReference,
    ): Int

    /**
     * Authenticates the user through the PAM stack.
     * For fprintd-enabled stacks, the sensor interaction is handled by fprintd;
     * the conversation callback receives PAM_PROMPT_ECHO_OFF and must reply with "".
     */
    fun pam_authenticate(pamh: Pointer, flags: Int): Int

    fun pam_end(pamh: Pointer, pamStatus: Int): Int

    companion object {
        val INSTANCE: LibPam by lazy {
            Native.load("pam", LibPam::class.java)
        }

        const val PAM_SUCCESS = 0
        const val PAM_AUTH_ERR = 7

        // Conversation message types sent to the conversation callback
        const val PAM_PROMPT_ECHO_OFF = 1   // password/fingerprint prompt (no echo)
        const val PAM_PROMPT_ECHO_ON = 2    // username prompt (with echo)
        const val PAM_ERROR_MSG = 3
        const val PAM_TEXT_INFO = 4
    }
}

/**
 * pam_conv structure — passed to pam_start.
 * [conv] is called by the PAM stack to exchange authentication tokens.
 */
@Structure.FieldOrder("conv", "appdata_ptr")
internal open class PamConv : Structure() {
    @JvmField var conv: PamConvCallback? = null
    @JvmField var appdata_ptr: Pointer? = null

    class ByReference : PamConv(), Structure.ByReference
}

/**
 * The PAM conversation function.
 * Called by the PAM stack with an array of [numMsg] pam_message pointers.
 * Must allocate a pam_response array and write it to [resp].
 *
 * pam_message layout: int msg_style (4 bytes) + char* msg (pointer)
 * pam_response layout: char* resp (pointer) + int resp_retcode (4 bytes, must be 0)
 */
internal interface PamConvCallback : Callback {
    fun invoke(
        numMsg: Int,
        msg: Pointer,             // struct pam_message** (array of pointers to pam_message)
        resp: PointerByReference, // struct pam_response** — caller must allocate with malloc
        appdata_ptr: Pointer?,
    ): Int
}
