package com.programmersbox.common.presentation.components.videoloader

import com.programmersbox.common.Network
import com.programmersbox.common.NetworkConnectionRepository
import dev.jordond.connectivity.Connectivity
import io.ktor.client.request.prepareGet
import io.ktor.http.isSuccess
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import okio.FileSystem
import okio.Path
import okio.Path.Companion.toPath
import okio.SYSTEM
import kotlin.time.Clock

private const val VideoThumbnailCacheDirectoryName = "video-thumbnails"
private const val VideoThumbnailCacheIndexFileName = "index.json"
private const val VideoThumbnailCacheFramesDirectoryName = "frames"
private const val DefaultVideoThumbnailCacheMaxSizeBytes = 64L * 1024L * 1024L

internal class VideoThumbnailLoadException(
    message: String,
    cause: Throwable? = null,
) : Exception(message, cause)

internal interface VideoThumbnailUrlResolver {
    suspend fun resolve(videoUrl: String): Result<String>
}

internal interface VideoThumbnailCacheDirectoryProvider {
    fun cacheDirectory(): Path
}

internal interface VideoThumbnailPlatformExtractor {
    suspend fun extract(
        videoUrl: String,
        framePositions: List<Float>,
    ): Result<List<ExtractedVideoThumbnailFrame>>
}

internal data class ExtractedVideoThumbnailFrame(
    val index: Int,
    val positionFraction: Float,
    val bytes: ByteArray,
    val fileExtension: String = "png",
)

internal class KtorVideoThumbnailUrlResolver(
    private val network: Network,
) : VideoThumbnailUrlResolver {

    override suspend fun resolve(videoUrl: String): Result<String> {
        if (videoUrl.isBlank()) {
            return Result.failure(VideoThumbnailLoadException("Video URL must not be blank"))
        }

        return try {
            Result.success(
                network.client.prepareGet(videoUrl).execute { response ->
                    if (!response.status.isSuccess()) {
                        throw VideoThumbnailLoadException(
                            message = "Video URL request failed with HTTP ${response.status.value}",
                        )
                    }
                    response.call.request.url.toString()
                }
            )
        } catch (cancellationException: CancellationException) {
            throw cancellationException
        } catch (throwable: Throwable) {
            Result.failure(
                VideoThumbnailLoadException(
                    message = "Unable to resolve video URL",
                    cause = throwable,
                )
            )
        }
    }
}

internal class DefaultVideoThumbnailFrameLoader(
    private val urlResolver: VideoThumbnailUrlResolver,
    private val diskCache: VideoThumbnailDiskCache,
    private val extractor: VideoThumbnailPlatformExtractor,
    private val networkConnectionRepository: NetworkConnectionRepository
) : VideoThumbnailFrameLoader {

    override suspend fun load(request: VideoThumbnailRequest): Result<VideoThumbnailResult> {
        diskCache.get(request)
            .getOrNull()
            ?.let { return Result.success(it) }

        val shouldPullFromNetwork = when (
            val status = networkConnectionRepository.connectivity.status()
        ) {
            is Connectivity.Status.Connected -> !status.isMetered
            Connectivity.Status.Disconnected -> false
        }

        if (!shouldPullFromNetwork) {
            return Result.failure(VideoThumbnailLoadException("No internet connection"))
        }

        val resolvedUrl = urlResolver.resolve(request.videoUrl)
            .getOrElse {
                return Result.failure(
                    VideoThumbnailLoadException(
                        message = it.message ?: "Unable to resolve video URL",
                        cause = it,
                    )
                )
            }

        val extractedFrames = extractor.extract(
            videoUrl = resolvedUrl,
            framePositions = request.framePositions,
        ).getOrElse {
            return Result.failure(
                VideoThumbnailLoadException(
                    message = it.message ?: "Unable to extract video thumbnails",
                    cause = it,
                )
            )
        }

        if (extractedFrames.size != request.frameCount) {
            return Result.failure(
                VideoThumbnailLoadException(
                    "Expected ${request.frameCount} frames but extracted ${extractedFrames.size}",
                )
            )
        }

        return diskCache.put(
            request = request,
            frames = extractedFrames,
        )
    }
}

internal class VideoThumbnailDiskCache(
    private val directoryProvider: VideoThumbnailCacheDirectoryProvider,
    private val fileSystem: FileSystem = FileSystem.SYSTEM,
    private val json: Json = Json {
        ignoreUnknownKeys = true
        encodeDefaults = true
        explicitNulls = false
    },
) {

    private val mutex = Mutex()

    suspend fun get(request: VideoThumbnailRequest): Result<VideoThumbnailResult?> = try {
        Result.success(
            mutex.withLock {
                ensureDirectories()

                val index = readIndex()
                val entry = index.entries.firstOrNull {
                    it.cacheKey == request.cacheKey.value && it.frames.size == request.frameCount
                } ?: return@withLock null

                val frames = mutableListOf<VideoThumbnailFrame>()
                for (frame in entry.frames.sortedBy(VideoThumbnailCacheFrameEntry::index)) {
                    val path = framesDirectory().child(frame.fileName)
                    val metadata = fileSystem.metadataOrNull(path)
                    if (metadata?.isRegularFile != true) {
                        deleteFrameFiles(entry)
                        writeIndex(index.remove(entry.cacheKey))
                        return@withLock null
                    }

                    frames += VideoThumbnailFrame(
                        index = frame.index,
                        positionFraction = frame.positionFraction,
                        cachedUri = path.toFileUri(),
                    )
                }

                writeIndex(
                    index.upsert(
                        entry.copy(lastAccessedEpochMillis = nowEpochMillis())
                    )
                )

                VideoThumbnailResult(request = request, frames = frames)
            }
        )
    } catch (cancellationException: CancellationException) {
        throw cancellationException
    } catch (throwable: Throwable) {
        Result.failure(
            VideoThumbnailLoadException(
                message = "Unable to read cached video thumbnails",
                cause = throwable,
            )
        )
    }

    suspend fun put(
        request: VideoThumbnailRequest,
        frames: List<ExtractedVideoThumbnailFrame>,
    ): Result<VideoThumbnailResult> = try {
        Result.success(
            mutex.withLock {
                ensureDirectories()

                val currentIndex = readIndex()
                val existingEntry =
                    currentIndex.entries.firstOrNull { it.cacheKey == request.cacheKey.value }
                val persistedFrames =
                    frames.sortedBy(ExtractedVideoThumbnailFrame::index).map { frame ->
                        val sanitizedExtension =
                            frame.fileExtension.trimStart('.').ifBlank { "png" }
                        val fileName =
                            "${request.cacheKey.frameId(frame.index)}.$sanitizedExtension"
                        val path = framesDirectory().child(fileName)

                        fileSystem.write(path) {
                            write(frame.bytes)
                        }

                        VideoThumbnailCacheFrameEntry(
                            index = frame.index,
                            positionFraction = frame.positionFraction,
                            fileName = fileName,
                            sizeBytes = fileSystem.metadata(path).size ?: frame.bytes.size.toLong(),
                        )
                    }

                val newEntry = VideoThumbnailCacheEntry(
                    cacheKey = request.cacheKey.value,
                    createdAtEpochMillis = existingEntry?.createdAtEpochMillis ?: nowEpochMillis(),
                    lastAccessedEpochMillis = nowEpochMillis(),
                    frames = persistedFrames,
                )

                deleteFrameFiles(
                    existingEntry,
                    keepFileNames = persistedFrames.map(VideoThumbnailCacheFrameEntry::fileName)
                        .toSet(),
                )

                val prunedIndex = currentIndex
                    .upsert(newEntry)
                    .prune(DefaultVideoThumbnailCacheMaxSizeBytes)

                prunedIndex.evictedEntries.forEach(::deleteFrameFiles)
                writeIndex(prunedIndex.index)

                VideoThumbnailResult(
                    request = request,
                    frames = persistedFrames.map { frame ->
                        VideoThumbnailFrame(
                            index = frame.index,
                            positionFraction = frame.positionFraction,
                            cachedUri = framesDirectory().child(frame.fileName).toFileUri(),
                        )
                    }
                )
            }
        )
    } catch (cancellationException: CancellationException) {
        throw cancellationException
    } catch (throwable: Throwable) {
        Result.failure(
            VideoThumbnailLoadException(
                message = "Unable to persist video thumbnails",
                cause = throwable,
            )
        )
    }

    private fun ensureDirectories() {
        fileSystem.createDirectories(rootDirectory())
        fileSystem.createDirectories(framesDirectory())
    }

    private fun readIndex(): VideoThumbnailCacheIndex {
        val indexFile = indexFile()
        val metadata = fileSystem.metadataOrNull(indexFile) ?: return VideoThumbnailCacheIndex()
        if (!metadata.isRegularFile) return VideoThumbnailCacheIndex()

        return try {
            json.decodeFromString<VideoThumbnailCacheIndex>(fileSystem.read(indexFile) { readUtf8() })
        } catch (_: Throwable) {
            fileSystem.delete(indexFile, mustExist = false)
            VideoThumbnailCacheIndex()
        }
    }

    private fun writeIndex(index: VideoThumbnailCacheIndex) {
        fileSystem.write(indexFile()) {
            writeUtf8(json.encodeToString(index))
        }
    }

    private fun deleteFrameFiles(
        entry: VideoThumbnailCacheEntry?,
        keepFileNames: Set<String> = emptySet(),
    ) {
        entry?.frames
            ?.filterNot { it.fileName in keepFileNames }
            ?.forEach { frame ->
                fileSystem.delete(framesDirectory().child(frame.fileName), mustExist = false)
            }
    }

    private fun rootDirectory(): Path =
        directoryProvider.cacheDirectory().child(VideoThumbnailCacheDirectoryName)

    private fun framesDirectory(): Path =
        rootDirectory().child(VideoThumbnailCacheFramesDirectoryName)

    private fun indexFile(): Path = rootDirectory().child(VideoThumbnailCacheIndexFileName)
}

@Serializable
internal data class VideoThumbnailCacheIndex(
    val entries: List<VideoThumbnailCacheEntry> = emptyList(),
) {

    fun upsert(entry: VideoThumbnailCacheEntry): VideoThumbnailCacheIndex = copy(
        entries = entries.filterNot { it.cacheKey == entry.cacheKey } + entry
    )

    fun remove(cacheKey: String): VideoThumbnailCacheIndex = copy(
        entries = entries.filterNot { it.cacheKey == cacheKey }
    )

    fun prune(maxSizeBytes: Long): PrunedVideoThumbnailCacheIndex {
        if (entries.isEmpty()) return PrunedVideoThumbnailCacheIndex(
            index = this,
            evictedEntries = emptyList()
        )

        val keptEntries = mutableListOf<VideoThumbnailCacheEntry>()
        val evictedEntries = mutableListOf<VideoThumbnailCacheEntry>()
        var currentSize = 0L

        entries.sortedByDescending(VideoThumbnailCacheEntry::lastAccessedEpochMillis)
            .forEach { entry ->
                val entrySize = entry.totalSizeBytes
                if (keptEntries.isEmpty() || currentSize + entrySize <= maxSizeBytes) {
                    keptEntries += entry
                    currentSize += entrySize
                } else {
                    evictedEntries += entry
                }
            }

        return PrunedVideoThumbnailCacheIndex(
            index = copy(entries = keptEntries.sortedByDescending(VideoThumbnailCacheEntry::lastAccessedEpochMillis)),
            evictedEntries = evictedEntries,
        )
    }
}

internal data class PrunedVideoThumbnailCacheIndex(
    val index: VideoThumbnailCacheIndex,
    val evictedEntries: List<VideoThumbnailCacheEntry>,
)

@Serializable
internal data class VideoThumbnailCacheEntry(
    val cacheKey: String,
    val createdAtEpochMillis: Long,
    val lastAccessedEpochMillis: Long,
    val frames: List<VideoThumbnailCacheFrameEntry>,
) {
    val totalSizeBytes: Long get() = frames.sumOf(VideoThumbnailCacheFrameEntry::sizeBytes)
}

@Serializable
internal data class VideoThumbnailCacheFrameEntry(
    val index: Int,
    val positionFraction: Float,
    val fileName: String,
    val sizeBytes: Long,
)

private fun nowEpochMillis(): Long = Clock.System.now().toEpochMilliseconds()

internal fun Path.toFileUri(): String = "file://$this"

private fun Path.child(name: String): Path = "${toString().trimEnd('/')}/$name".toPath()
