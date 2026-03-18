package com.programmersbox.common.presentation.components.videoloader

import kotlinx.coroutines.CancellationException
import okio.Path
import okio.Path.Companion.toPath
import java.io.File

internal class JvmVideoThumbnailCacheDirectoryProvider : VideoThumbnailCacheDirectoryProvider {
    override fun cacheDirectory(): Path {
        val root = File(System.getProperty("user.home"), ".civitaimodelbrowser/cache")
        if (!root.exists()) root.mkdirs()
        return root.absolutePath.toPath()
    }
}

internal class JvmVideoThumbnailPlatformExtractor : VideoThumbnailPlatformExtractor {
    override suspend fun extract(
        videoUrl: String,
        framePositions: List<Float>,
    ): Result<List<ExtractedVideoThumbnailFrame>> = try {
        Result.failure(
            VideoThumbnailLoadException(
                "Desktop frame extraction is unavailable with the current bundled media backend",
            )
        )
    } catch (cancellationException: CancellationException) {
        throw cancellationException
    }
}
