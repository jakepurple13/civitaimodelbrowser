package com.programmersbox.common.presentation.components.videoloader

import androidx.compose.ui.graphics.toComposeImageBitmap
import ca.gosyer.appdirs.AppDirs
import io.github.vinceglb.filekit.dialogs.compose.util.encodeToByteArray
import kotlinx.coroutines.CancellationException
import okio.Path
import okio.Path.Companion.toPath
import org.bytedeco.javacv.FFmpegFrameGrabber
import org.bytedeco.javacv.Java2DFrameConverter
import java.io.File
import java.net.URL

internal class JvmVideoThumbnailCacheDirectoryProvider(
    private val appDirs: AppDirs
) : VideoThumbnailCacheDirectoryProvider {
    override fun cacheDirectory(): Path {
        val root = File(appDirs.getUserDataDir(), ".civitaimodelbrowser/cache")
        if (!root.exists()) root.mkdirs()
        return root.absolutePath.toPath()
    }
}

internal class JvmVideoThumbnailPlatformExtractor : VideoThumbnailPlatformExtractor {
    override suspend fun extract(
        videoUrl: String,
        framePositions: List<Float>,
    ): Result<List<ExtractedVideoThumbnailFrame>> = try {
        val grabber = FFmpegFrameGrabber(URL(videoUrl))
        grabber.start()
        val converter = Java2DFrameConverter()
        val list = mutableListOf<ExtractedVideoThumbnailFrame>()
        for (i in 0 until grabber.lengthInFrames) {
            val frame = grabber.grabImage()
            if (frame != null && framePositions.contains(i.toFloat() / grabber.lengthInFrames)) {
                val image = converter.convert(frame)
                list.add(
                    ExtractedVideoThumbnailFrame(
                        index = i,
                        positionFraction = framePositions[i],
                        bytes = image.toComposeImageBitmap().encodeToByteArray()
                    )
                )
            }
        }
        grabber.stop()

        Result.success(list)
    } catch (cancellationException: CancellationException) {
        throw cancellationException
    } catch (e: Exception) {
        Result.failure(e)
    }
}
