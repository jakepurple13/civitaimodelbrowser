package com.programmersbox.common.presentation.components.videoloader

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.CircularWavyProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.Stable
import androidx.compose.runtime.State
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import com.programmersbox.resources.Res
import com.programmersbox.resources.civitai_logo
import io.kamel.image.KamelImage
import io.kamel.image.asyncPainterResource
import kotlinx.coroutines.delay
import org.jetbrains.compose.resources.ExperimentalResourceApi
import org.jetbrains.compose.resources.painterResource

const val MinVideoThumbnailFrameCount = 3
const val MaxVideoThumbnailFrameCount = 5
const val DefaultVideoThumbnailFrameCount = 5

private const val VideoThumbnailPreviewFrameDurationMillis = 750L

@Composable
fun VideoThumbnailLoader(
    videoUrl: String,
    modifier: Modifier = Modifier,
    contentScale: ContentScale = ContentScale.Crop,
    placeholderContent: @Composable BoxScope.() -> Unit = { DefaultVideoThumbnailPlaceholder() },
    errorContent: @Composable BoxScope.() -> Unit = { DefaultVideoThumbnailError() },
) {
    val uiState by rememberVideoThumbnailUiState(
        videoUrl = videoUrl,
    )
    val previewFrameUris = (uiState as? VideoThumbnailUiState.Success)
        ?.result
        ?.previewFrameUris()
        .orEmpty()

    Box(modifier = modifier, contentAlignment = Alignment.Center) {
        when (val renderModel = uiState.toRenderModel()) {
            VideoThumbnailRenderModel.Loading -> placeholderContent()
            is VideoThumbnailRenderModel.Error -> errorContent()
            is VideoThumbnailRenderModel.Success -> {
                val previewFrameIndex = rememberVideoThumbnailPreviewFrameIndex(previewFrameUris)
                val previewFrameUris = renderModel
                    .result
                    .previewFrameUris()
                val frameUri = previewFrameUris[previewFrameIndex % previewFrameUris.size]

                KamelImage(
                    resource = { asyncPainterResource(frameUri) },
                    onLoading = { placeholderContent() },
                    onFailure = { errorContent() },
                    contentScale = contentScale,
                    contentDescription = null,
                    modifier = Modifier.fillMaxSize(),
                )
            }
        }
    }
}

@Composable
private fun rememberVideoThumbnailPreviewFrameIndex(previewFrameUris: List<String>): Int {
    var previewFrameIndex by remember(previewFrameUris) { mutableIntStateOf(0) }

    LaunchedEffect(previewFrameUris) {
        previewFrameIndex = 0
        if (previewFrameUris.size <= 1) return@LaunchedEffect

        while (true) {
            delay(VideoThumbnailPreviewFrameDurationMillis)
            previewFrameIndex = (previewFrameIndex + 1) % previewFrameUris.size
        }
    }

    return previewFrameIndex
}

@Composable
fun rememberVideoThumbnailUiState(
    videoUrl: String,
    frameLoader: VideoThumbnailFrameLoader? = null,
    frameCount: Int = DefaultVideoThumbnailFrameCount,
): State<VideoThumbnailUiState> {
    val resolvedFrameLoader = frameLoader ?: LocalVideoThumbnailFrameLoader.current

    return produceState<VideoThumbnailUiState>(
        initialValue = VideoThumbnailUiState.Loading,
        key1 = videoUrl,
        key2 = resolvedFrameLoader,
        key3 = frameCount,
    ) {
        value = if (videoUrl.isBlank()) {
            VideoThumbnailUiState.Error("videoUrl must not be blank")
        } else {
            resolvedFrameLoader
                .load(VideoThumbnailRequest(videoUrl = videoUrl, frameCount = frameCount))
                .fold(
                    onSuccess = VideoThumbnailUiState::Success,
                    onFailure = {
                        VideoThumbnailUiState.Error(
                            it.message ?: "Unable to load video thumbnails"
                        )
                    },
                )
        }
    }
}

@Immutable
data class VideoThumbnailRequest(
    val videoUrl: String,
    val frameCount: Int = DefaultVideoThumbnailFrameCount,
    val cacheKey: VideoThumbnailCacheKey = VideoThumbnailCacheKey.fromVideoUrl(videoUrl),
) {
    init {
        require(frameCount in MinVideoThumbnailFrameCount..MaxVideoThumbnailFrameCount) {
            "frameCount must be between $MinVideoThumbnailFrameCount and $MaxVideoThumbnailFrameCount"
        }
    }

    val framePositions: List<Float> = defaultFramePositions(frameCount)
}

@Immutable
data class VideoThumbnailResult(
    val request: VideoThumbnailRequest,
    val frames: List<VideoThumbnailFrame>,
) {
    init {
        require(frames.size == request.frameCount) { "frames must match the requested frameCount" }
    }

    val primaryFrame: VideoThumbnailFrame get() = frames.first()
}

@Immutable
data class VideoThumbnailFrame(
    val index: Int,
    val positionFraction: Float,
    val cachedUri: String,
) {
    init {
        require(index >= 0) { "index must be >= 0" }
        require(positionFraction in 0f..1f) { "positionFraction must be between 0f and 1f" }
        require(cachedUri.isNotBlank()) { "cachedUri must not be blank" }
    }
}

@Immutable
data class VideoThumbnailCacheKey(
    val value: String,
) {
    init {
        require(value.isNotBlank()) { "value must not be blank" }
    }

    fun frameId(index: Int): String {
        require(index >= 0) { "index must be >= 0" }
        return "$value-frame-$index"
    }

    companion object {
        fun fromVideoUrl(videoUrl: String): VideoThumbnailCacheKey =
            VideoThumbnailCacheKey("video-thumbnail-${stableVideoThumbnailHash(videoUrl)}")
    }
}

@Stable
sealed interface VideoThumbnailUiState {
    @Immutable
    data object Loading : VideoThumbnailUiState

    @Immutable
    data class Success(val result: VideoThumbnailResult) : VideoThumbnailUiState

    @Immutable
    data class Error(val message: String) : VideoThumbnailUiState
}

@Stable
interface VideoThumbnailFrameLoader {
    suspend fun load(request: VideoThumbnailRequest): Result<VideoThumbnailResult>
}

@Immutable
internal sealed interface VideoThumbnailRenderModel {
    @Immutable
    data object Loading : VideoThumbnailRenderModel

    @Immutable
    data class Success(
        val result: VideoThumbnailResult,
    ) : VideoThumbnailRenderModel

    @Immutable
    data class Error(val message: String) : VideoThumbnailRenderModel
}

internal fun VideoThumbnailUiState.toRenderModel(): VideoThumbnailRenderModel =
    when (this) {
        VideoThumbnailUiState.Loading -> VideoThumbnailRenderModel.Loading
        is VideoThumbnailUiState.Error -> VideoThumbnailRenderModel.Error(message)
        is VideoThumbnailUiState.Success -> VideoThumbnailRenderModel.Success(result = result)
    }

internal fun VideoThumbnailResult.previewFrameUris(): List<String> =
    frames.map(VideoThumbnailFrame::cachedUri).distinct()

val LocalVideoThumbnailFrameLoader = staticCompositionLocalOf<VideoThumbnailFrameLoader> {
    object : VideoThumbnailFrameLoader {
        override suspend fun load(request: VideoThumbnailRequest): Result<VideoThumbnailResult> {
            return Result.failure(NotImplementedError("Platform VideoThumbnailFrameLoader is not installed yet"))
        }
    }
}

private fun defaultFramePositions(frameCount: Int): List<Float> =
    List(frameCount) { index -> (index + 1f) / (frameCount + 1f) }

private fun stableVideoThumbnailHash(videoUrl: String): String {
    var hash = 0xcbf29ce484222325uL
    val prime = 0x100000001b3uL
    videoUrl.encodeToByteArray().forEach { byte ->
        hash = (hash xor byte.toUByte().toULong()) * prime
    }
    return hash.toString(16).padStart(16, '0')
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
private fun BoxScope.DefaultVideoThumbnailPlaceholder() {
    VideoThumbnailStatusContainer {
        CircularWavyProgressIndicator()
        Text(
            text = "Loading preview...",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@OptIn(ExperimentalResourceApi::class)
@Composable
private fun BoxScope.DefaultVideoThumbnailError() {
    VideoThumbnailStatusContainer {
        Image(
            painter = painterResource(Res.drawable.civitai_logo),
            contentDescription = null,
            modifier = Modifier.size(72.dp),
        )
        Text(
            text = "Preview unavailable",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Composable
private fun BoxScope.VideoThumbnailStatusContainer(content: @Composable () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.7f)),
        contentAlignment = Alignment.Center,
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.padding(16.dp),
        ) {
            content()
        }
    }
}