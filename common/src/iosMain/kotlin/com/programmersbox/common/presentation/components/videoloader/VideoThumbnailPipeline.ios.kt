@file:OptIn(ExperimentalForeignApi::class)

package com.programmersbox.common.presentation.components.videoloader


import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.addressOf
import kotlinx.cinterop.usePinned
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.withContext
import okio.Path
import okio.Path.Companion.toPath
import platform.AVFoundation.AVAsset
import platform.AVFoundation.AVAssetImageGenerator
import platform.CoreMedia.CMTimeGetSeconds
import platform.CoreMedia.CMTimeMakeWithSeconds
import platform.Foundation.NSCachesDirectory
import platform.Foundation.NSData
import platform.Foundation.NSFileManager
import platform.Foundation.NSURL
import platform.Foundation.NSUserDomainMask
import platform.UIKit.UIImage
import platform.UIKit.UIImagePNGRepresentation
import platform.posix.memcpy

internal class IosVideoThumbnailCacheDirectoryProvider : VideoThumbnailCacheDirectoryProvider {
    override fun cacheDirectory(): Path {
        val directory = requireNotNull(
            NSFileManager.defaultManager.URLForDirectory(
                directory = NSCachesDirectory,
                inDomain = NSUserDomainMask,
                appropriateForURL = null,
                create = true,
                error = null,
            )?.path
        )
        return directory.toPath()
    }
}

internal class IosVideoThumbnailPlatformExtractor : VideoThumbnailPlatformExtractor {

    override suspend fun extract(
        videoUrl: String,
        framePositions: List<Float>,
    ): Result<List<ExtractedVideoThumbnailFrame>> = withContext(Dispatchers.IO) {
        try {
            val assetUrl = requireNotNull(NSURL.URLWithString(videoUrl)) { "Invalid video URL" }
            val asset = AVAsset.assetWithURL(assetUrl)
            val durationSeconds = CMTimeGetSeconds(asset.duration)
            require(durationSeconds > 0.0) { "Video duration was unavailable" }

            val generator = AVAssetImageGenerator.assetImageGeneratorWithAsset(asset).apply {
                appliesPreferredTrackTransform = true
            }

            val frames = framePositions.mapIndexedNotNull { index, positionFraction ->
                val cgImage = generator.copyCGImageAtTime(
                    requestedTime = CMTimeMakeWithSeconds(
                        seconds = durationSeconds * positionFraction.toDouble(),
                        preferredTimescale = 600,
                    ),
                    actualTime = null,
                    error = null,
                ) ?: return@mapIndexedNotNull null

                val pngData = UIImagePNGRepresentation(UIImage.imageWithCGImage(cgImage))
                    ?: return@mapIndexedNotNull null

                ExtractedVideoThumbnailFrame(
                    index = index,
                    positionFraction = positionFraction,
                    bytes = pngData.toByteArray(),
                )
            }

            Result.success(frames)
        } catch (cancellationException: CancellationException) {
            throw cancellationException
        } catch (throwable: Throwable) {
            Result.failure(
                VideoThumbnailLoadException(
                    message = "iOS frame extraction failed",
                    cause = throwable,
                )
            )
        }
    }
}

@OptIn(ExperimentalForeignApi::class)
private fun NSData.toByteArray(): ByteArray {
    if (length.toInt() == 0) return ByteArray(0)
    return ByteArray(length.toInt()).apply {
        usePinned { pinned ->
            memcpy(pinned.addressOf(0), requireNotNull(bytes), length)
        }
    }
}
