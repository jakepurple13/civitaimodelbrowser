package com.programmersbox.common.qrcode

import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asSkiaBitmap
import io.github.vinceglb.filekit.FileKit
import io.github.vinceglb.filekit.dialogs.compose.util.encodeToByteArray
import io.github.vinceglb.filekit.dialogs.openFileSaver
import io.github.vinceglb.filekit.write
import kotlinx.cinterop.BetaInteropApi
import kotlinx.cinterop.CFunction
import kotlinx.cinterop.COpaquePointer
import kotlinx.cinterop.CPointer
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.addressOf
import kotlinx.cinterop.usePinned
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.jetbrains.skia.EncodedImageFormat
import org.jetbrains.skia.Image
import platform.CoreGraphics.CGBitmapContextCreate
import platform.CoreGraphics.CGBitmapContextCreateImage
import platform.CoreGraphics.CGColorSpaceCreateDeviceRGB
import platform.CoreGraphics.CGImageAlphaInfo
import platform.CoreGraphics.CGImageRef
import platform.CoreGraphics.kCGBitmapByteOrder32Big
import platform.Foundation.NSData
import platform.Foundation.create
import platform.Foundation.dataWithBytes
import platform.UIKit.UIActivityViewController
import platform.UIKit.UIApplication
import platform.UIKit.UIDevice
import platform.UIKit.UIUserInterfaceIdiomPad
import platform.UIKit.popoverPresentationController
import platform.Vision.VNBarcodeObservation
import platform.Vision.VNBarcodeSymbologyQR
import platform.Vision.VNDetectBarcodesRequest
import platform.Vision.VNImageRequestHandler

actual class QrCodeRepository {
    @OptIn(ExperimentalForeignApi::class)
    actual suspend fun getInfoFromQRCode(bitmap: ImageBitmap): Result<List<String>> =
        withContext(Dispatchers.Default) {
            //TODO: Get working. Doesn't work on simulator
            try {
                // 1. Encode to PNG. This is slower than raw pixels but MUCH more stable
                // because it includes the headers Vision uses to set up the inference context.
                val skiaBitmap = bitmap.asSkiaBitmap()
                val skiaImage = Image.makeFromBitmap(skiaBitmap)
                val encodedData = skiaImage.encodeToData(EncodedImageFormat.PNG)
                    ?: return@withContext Result.failure(Exception("Encoding failed"))

                val byteArray = encodedData.bytes
                val nsData = byteArray.usePinned { pinned ->
                    NSData.dataWithBytes(pinned.addressOf(0), byteArray.size.toULong())
                }

                val deferredResults = CompletableDeferred<List<String>>()

                // 2. Setup Request
                val barcodeRequest = VNDetectBarcodesRequest { request, error ->
                    if (error != null) {
                        println("Error: $error")
                        println("Error: ${error.localizedFailureReason}")
                        println("Error: ${error.localizedRecoverySuggestion}")
                        println("Error: ${error.localizedDescription}")
                        deferredResults.completeExceptionally(Exception(error.localizedDescription))
                        return@VNDetectBarcodesRequest
                    }
                    val results = request?.results()
                        ?.filterIsInstance<VNBarcodeObservation>()
                        ?.mapNotNull { it.payloadStringValue } ?: emptyList()
                    deferredResults.complete(results)
                }

                // Explicitly set the symbology to QR
                barcodeRequest.setSymbologies(listOf(VNBarcodeSymbologyQR))

                // 3. Initialize the handler with Data
                // IMPORTANT: On M3, if emptyMap() fails, pass null for options
                val handler = VNImageRequestHandler(data = nsData, options = emptyMap<Any?, Any?>())

                // 4. Perform the request
                handler.performRequests(listOf(barcodeRequest), null)

                val finalResults = deferredResults.await()
                Result.success(finalResults)

            } catch (e: Exception) {
                println("QR Scan Error: ${e.message}")
                Result.failure(e)
            }
        }

    @OptIn(ExperimentalForeignApi::class)
    private fun createCGImageFromPixels(pixels: ByteArray, width: Int, height: Int): CGImageRef? {
        val releaseDescriptor: CPointer<CFunction<(COpaquePointer?, COpaquePointer?, Long) -> Unit>>? =
            null

        val colorSpace = CGColorSpaceCreateDeviceRGB()
        val bitmapInfo = CGImageAlphaInfo.kCGImageAlphaLast.value or kCGBitmapByteOrder32Big

        return pixels.usePinned { pinned ->
            val context = CGBitmapContextCreate(
                data = pinned.addressOf(0),
                width = width.toULong(),
                height = height.toULong(),
                bitsPerComponent = 8u,
                bytesPerRow = (width * 4).toULong(),
                space = colorSpace,
                bitmapInfo = bitmapInfo
            )
            CGBitmapContextCreateImage(context)
        }
    }

    actual suspend fun shareImage(
        bitmap: ImageBitmap,
        title: String
    ) {
        val activityController = UIActivityViewController(
            activityItems = listOf(
                bitmap
                    .encodeToByteArray()
                    .toNSData(),
                title
            ),
            applicationActivities = null
        )

        // Find the current root view controller to present the activity sheet
        val rootViewController = UIApplication.sharedApplication.keyWindow?.rootViewController

        // Required for iPad to avoid a crash (popover presentation)
        if (UIDevice.currentDevice.userInterfaceIdiom == UIUserInterfaceIdiomPad) {
            activityController.popoverPresentationController?.sourceView = rootViewController?.view
        }

        rootViewController?.presentViewController(
            activityController,
            animated = true,
            completion = null
        )
    }

    actual suspend fun saveImage(
        bitmap: ImageBitmap,
        title: String
    ) {
        FileKit.openFileSaver(
            suggestedName = title,
            extension = "png",
        )?.write(bitmap.encodeToByteArray())
    }

    // Helper extension function to convert Kotlin ByteArray to NSData
    @OptIn(ExperimentalForeignApi::class, BetaInteropApi::class)
    fun ByteArray.toNSData(): NSData? = usePinned { pinned ->
        NSData.create(bytes = pinned.addressOf(0), length = size.toULong())
    }

    actual suspend fun shareUrl(url: String, title: String) {
        val activityController = UIActivityViewController(
            activityItems = listOf(url, title),
            applicationActivities = null
        )

        // Find the current root view controller to present the activity sheet
        val rootViewController = UIApplication.sharedApplication.keyWindow?.rootViewController

        // Required for iPad to avoid a crash (popover presentation)
        if (UIDevice.currentDevice.userInterfaceIdiom == UIUserInterfaceIdiomPad) {
            activityController.popoverPresentationController?.sourceView = rootViewController?.view
        }

        rootViewController?.presentViewController(
            activityController,
            animated = true,
            completion = null
        )
    }
}