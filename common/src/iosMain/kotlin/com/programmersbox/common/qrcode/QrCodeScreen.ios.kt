package com.programmersbox.common.qrcode

import androidx.compose.ui.graphics.ImageBitmap
import io.github.vinceglb.filekit.dialogs.compose.util.encodeToByteArray
import kotlinx.cinterop.BetaInteropApi
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.addressOf
import kotlinx.cinterop.usePinned
import kotlinx.coroutines.runBlocking
import platform.CoreImage.CIDetector
import platform.CoreImage.CIDetectorAccuracyHigh
import platform.CoreImage.CIDetectorTypeQRCode
import platform.CoreImage.CIImage
import platform.Foundation.NSData
import platform.Foundation.create
import platform.Photos.PHAssetChangeRequest
import platform.Photos.PHAuthorizationStatusAuthorized
import platform.Photos.PHPhotoLibrary
import platform.Photos.PHPhotoLibrary.Companion.sharedPhotoLibrary
import platform.UIKit.UIActivityViewController
import platform.UIKit.UIApplication
import platform.UIKit.UIDevice
import platform.UIKit.UIImage
import platform.UIKit.UIUserInterfaceIdiomPad
import platform.UIKit.popoverPresentationController

actual class QrCodeRepository {
    actual suspend fun getInfoFromQRCode(bitmap: ImageBitmap): Result<List<String>> {
        val detect = CIDetector()
        val items = bitmap
            .encodeToByteArray()
            .toNSData()
            ?.let { CIImage(it) }
            ?.let {
                detect.featuresInImage(
                    it,
                    options = mapOf(
                        "CIDetectorTypeQRCode" to CIDetectorTypeQRCode,
                        "CIDetectorAccuracy" to CIDetectorAccuracyHigh
                    )
                )
            }
            ?.mapNotNull { it?.toString() }
            ?: return Result.failure(Exception("No QR Code Found"))

        return Result.success(items)
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
        PHPhotoLibrary.requestAuthorization { status ->
            if (status == PHAuthorizationStatusAuthorized) {
                // Convert Kotlin ByteArray to NSData
                val nsData = runBlocking {
                    bitmap
                        .encodeToByteArray()
                        .toNSData()
                }
                val img = nsData?.let { UIImage(data = it) }

                if (img != null) {
                    sharedPhotoLibrary().performChanges(
                        changeBlock = {
                            // Save the image
                            PHAssetChangeRequest.creationRequestForAssetFromImage(img)
                        },
                        completionHandler = { isSuccess, error ->
                            if (isSuccess) {
                                println("Image saved successfully!")
                            } else {
                                println("Error saving image: $error")
                            }
                        }
                    )
                }
            }
        }
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