package com.programmersbox.common.presentation.components.videoloader

import android.content.Context
import android.graphics.Bitmap
import android.media.MediaMetadataRetriever
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okio.Path
import okio.Path.Companion.toPath
import java.io.ByteArrayOutputStream

internal class AndroidVideoThumbnailCacheDirectoryProvider(
    private val context: Context,
) : VideoThumbnailCacheDirectoryProvider {
    override fun cacheDirectory(): Path = context.filesDir.resolve("cache").absolutePath.toPath()
}

internal class AndroidVideoThumbnailPlatformExtractor : VideoThumbnailPlatformExtractor {

    override suspend fun extract(
        videoUrl: String,
        framePositions: List<Float>,
    ): Result<List<ExtractedVideoThumbnailFrame>> = withContext(Dispatchers.IO) {
        try {
            val retriever = MediaMetadataRetriever()
            try {
                retriever.setDataSource(videoUrl, emptyMap())

                val durationMillis = retriever
                    .extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION)
                    ?.toLongOrNull()
                    ?.takeIf { it > 0L }
                    ?: throw VideoThumbnailLoadException("Video duration was unavailable")

                val frames = framePositions.mapIndexedNotNull { index, positionFraction ->
                    val timeMicros = (durationMillis * positionFraction * 1_000L).toLong()
                    retriever.getFrameAtTime(
                        timeMicros,
                        MediaMetadataRetriever.OPTION_CLOSEST_SYNC,
                    )?.let { bitmap ->
                        bitmap.use { capturedBitmap ->
                            ExtractedVideoThumbnailFrame(
                                index = index,
                                positionFraction = positionFraction,
                                bytes = capturedBitmap.toPngBytes(),
                            )
                        }
                    }
                }

                Result.success(frames)
            } finally {
                retriever.release()
            }
        } catch (cancellationException: CancellationException) {
            throw cancellationException
        } catch (throwable: Throwable) {
            Result.failure(
                VideoThumbnailLoadException(
                    message = "Android frame extraction failed",
                    cause = throwable,
                )
            )
        }
    }
}

private inline fun <T> Bitmap.use(block: (Bitmap) -> T): T = try {
    block(this)
} finally {
    recycle()
}

private fun Bitmap.toPngBytes(): ByteArray {
    val stream = ByteArrayOutputStream()
    check(compress(Bitmap.CompressFormat.PNG, 100, stream)) { "Unable to encode frame bitmap" }
    return stream.toByteArray()
}
