package com.programmersbox.common.presentation.components.biometric

import com.programmersbox.common.presentation.components.biometric.jna.CoreFoundation
import com.programmersbox.common.presentation.components.biometric.jna.LAEvalCallback
import com.programmersbox.common.presentation.components.biometric.jna.ObjCRuntime
import com.sun.jna.CallbackReference
import com.sun.jna.Memory
import com.sun.jna.Pointer
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit

internal class MacOsBiometricAuthenticator : PlatformBiometricAuthenticator {

    override fun authenticateBlocking(title: String, subtitle: String): BiometricResult {
        return try {
            performAuthentication(subtitle)
        } catch (e: Exception) {
            BiometricResult.Error(e.message ?: "macOS LAContext error")
        }
    }

    private fun performAuthentication(reason: String): BiometricResult {
        val rt = ObjCRuntime.INSTANCE
        val cf = CoreFoundation.INSTANCE

        // [LAContext new]
        val laContextClass = rt.objc_getClass("LAContext")
        val newSel = rt.sel_registerName("new")
        val context = rt.objc_msgSend(laContextClass, newSel)

        // [context canEvaluatePolicy:LAPolicyDeviceOwnerAuthentication error:nil]
        val canEvalSel = rt.sel_registerName("canEvaluatePolicy:error:")
        val canEval = rt.objc_msgSend(
            context, canEvalSel,
            ObjCRuntime.LA_POLICY_DEVICE_OWNER_AUTHENTICATION,
            Pointer.NULL
        )
        if (com.sun.jna.Pointer.nativeValue(canEval) == 0L) {
            return BiometricResult.Error("LAContext cannot evaluate policy (no biometrics/password enrolled)")
        }

        val latch = CountDownLatch(1)
        var result: BiometricResult = BiometricResult.Failure

        // Create CFString for the reason
        val cfReason = cf.CFStringCreateWithCString(null, reason, CoreFoundation.kCFStringEncodingUTF8)

        // Build the Objective-C reply block on the heap.
        // Block layout (64-bit, 32 bytes total):
        //   offset  0: void *isa           (8 bytes) — _NSConcreteMallocBlock (heap-allocated block)
        //   offset  8: int32_t flags       (4 bytes) — 0
        //   offset 12: int32_t reserved    (4 bytes) — 0
        //   offset 16: void (*invoke)(…)   (8 bytes) — function pointer
        //   offset 24: Block_descriptor*   (8 bytes)
        // Block_descriptor_1 (16 bytes):
        //   offset  0: uintptr_t reserved  (8 bytes) — 0
        //   offset  8: uintptr_t size      (8 bytes) — 32 (block struct size)

        val callback = object : LAEvalCallback {
            override fun invoke(blockPtr: Pointer, success: Byte, error: Pointer?) {
                result = if (success != 0.toByte()) BiometricResult.Success else BiometricResult.Failure
                cf.CFRelease(cfReason)
                latch.countDown()
            }
        }

        val fnPtr = CallbackReference.getFunctionPointer(callback)

        val descriptor = Memory(16L)
        descriptor.setLong(0, 0L)   // reserved
        descriptor.setLong(8, 32L)  // size of block struct

        val block = Memory(32L)
        block.setPointer(0, ObjCRuntime.nsConcreteMallocBlock) // isa
        block.setInt(8, 0)                                      // flags
        block.setInt(12, 0)                                     // reserved
        block.setPointer(16, fnPtr)                             // invoke
        block.setPointer(24, descriptor)                        // descriptor

        // [context evaluatePolicy:LAPolicyDeviceOwnerAuthentication localizedReason:reason reply:block]
        val evalSel = rt.sel_registerName("evaluatePolicy:localizedReason:reply:")
        rt.objc_msgSend(
            context, evalSel,
            ObjCRuntime.LA_POLICY_DEVICE_OWNER_AUTHENTICATION,
            cfReason,
            block
        )

        val completed = latch.await(30, TimeUnit.SECONDS)

        // Release the LAContext — JNA calls do not benefit from ARC, so we must MRR release manually.
        val releaseSel = rt.sel_registerName("release")
        rt.objc_msgSend(context, releaseSel)

        // Keep JNA-managed objects alive until after await to prevent premature GC.
        // Reference.reachabilityFence (Java 9+) is the correct idiom for this.
        java.lang.ref.Reference.reachabilityFence(callback)
        java.lang.ref.Reference.reachabilityFence(block)
        java.lang.ref.Reference.reachabilityFence(descriptor)

        if (!completed) return BiometricResult.Error("Authentication timed out")
        return result
    }
}
