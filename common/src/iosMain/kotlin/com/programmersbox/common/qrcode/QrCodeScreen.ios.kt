package com.programmersbox.common.qrcode

import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asSkiaBitmap
import io.github.vinceglb.filekit.FileKit
import io.github.vinceglb.filekit.dialogs.compose.util.encodeToByteArray
import io.github.vinceglb.filekit.dialogs.openFileSaver
import io.github.vinceglb.filekit.write
import kotlinx.cinterop.BetaInteropApi
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.addressOf
import kotlinx.cinterop.usePinned
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import platform.Foundation.NSData
import platform.Foundation.create
import platform.UIKit.UIActivityViewController
import platform.UIKit.UIApplication
import platform.UIKit.UIDevice
import platform.UIKit.UIUserInterfaceIdiomPad
import platform.UIKit.popoverPresentationController
import zxingcpp.BarcodeFormat
import zxingcpp.BarcodeReader
import zxingcpp.ImageFormat
import zxingcpp.ImageView

actual class QrCodeRepository {
    @OptIn(ExperimentalForeignApi::class)
    actual suspend fun getInfoFromQRCode(
        bitmap: ImageBitmap
    ): Result<List<String>> = withContext(Dispatchers.Default) {
        return@withContext try {
            val imageView = ImageView(
                data = bitmap.asSkiaBitmap().readPixels()!!,
                width = bitmap.width,
                height = bitmap.height,
                format = ImageFormat.RGBA
            )
            val barcodeReader = BarcodeReader().apply {
                formats = setOf(BarcodeFormat.QRCode)
                tryHarder = true
                maxNumberOfSymbols = 3
            }
            Result.success(barcodeReader.read(imageView).mapNotNull { it.text })
        } catch (e: Exception) {
            println("QR Scan Error: ${e.message}")
            e.printStackTrace()
            Result.failure(e)
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