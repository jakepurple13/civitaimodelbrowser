package com.programmersbox.common.presentation.components.biometric.jna

import com.sun.jna.Callback
import com.sun.jna.Library
import com.sun.jna.Native
import com.sun.jna.NativeLibrary
import com.sun.jna.Pointer

/**
 * JNA bindings to the Objective-C runtime.
 * Loaded from "objc" → resolves to /usr/lib/libobjc.A.dylib on macOS.
 */
internal interface ObjCRuntime : Library {
    /** Returns the class object for the named ObjC class. */
    fun objc_getClass(name: String): Pointer

    /** Registers or returns the selector (SEL) for the given method name string. */
    fun sel_registerName(str: String): Pointer

    /**
     * Sends a message to an ObjC object.
     * Return type is Pointer; cast as needed. For boolean returns, check != Pointer.NULL.
     */
    fun objc_msgSend(receiver: Pointer, selector: Pointer, vararg args: Any?): Pointer

    companion object {
        val INSTANCE: ObjCRuntime by lazy {
            Native.load("objc", ObjCRuntime::class.java)
        }

        /** Address of _NSConcreteGlobalBlock — used as the `isa` field in block structs. */
        val nsConcreteGlobalBlock: Pointer by lazy {
            NativeLibrary.getInstance("objc").getGlobalVariableAddress("_NSConcreteGlobalBlock")
        }

        // LAContext policy constants
        /** LAPolicyDeviceOwnerAuthentication — biometrics with automatic password fallback. */
        const val LA_POLICY_DEVICE_OWNER_AUTHENTICATION = 2L
    }
}

/**
 * JNA bindings to CoreFoundation.
 * Used to create CFString instances for the LAContext reason string.
 */
internal interface CoreFoundation : Library {
    fun CFStringCreateWithCString(alloc: Pointer?, cStr: String, encoding: Int): Pointer
    fun CFRelease(cf: Pointer)

    companion object {
        val INSTANCE: CoreFoundation by lazy {
            Native.load("CoreFoundation", CoreFoundation::class.java)
        }
        const val kCFStringEncodingUTF8 = 0x08000100
    }
}

/**
 * JNA Callback type for the LAContext evaluatePolicy reply block.
 * ObjC calling convention passes the block pointer as the first argument.
 * BOOL maps to Byte on 64-bit (0 = false, non-zero = true).
 */
internal interface LAEvalCallback : Callback {
    fun invoke(blockPtr: Pointer, success: Byte, error: Pointer?)
}
