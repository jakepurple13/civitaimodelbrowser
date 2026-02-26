package com.programmersbox.common.presentation.components

import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import chaintech.videoplayer.ui.preview.VideoPreviewComposable
import androidx.navigation3.ui.LocalNavAnimatedContentScope
import com.programmersbox.common.LocalSharedTransitionScope
import com.programmersbox.common.ModelImage

@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
fun ModelCard(
    imageUrl: String,
    name: String,
    type: String,
    isNsfw: Boolean,
    showNsfw: Boolean,
    blurStrength: Dp,
    isFavorite: Boolean,
    isBlacklisted: Boolean,
    shouldShowMedia: Boolean,
    modifier: Modifier = Modifier,
    modelId: String? = null,
    blurHash: String? = null,
    creatorImage: String? = null,
    onLongClick: () -> Unit = {},
    onClick: () -> Unit = {},
    onDoubleClick: (() -> Unit)? = null,
) {
    val sharedTransitionScope = LocalSharedTransitionScope.current
    val animatedVisibilityScope = LocalNavAnimatedContentScope.current
    val sharedModifier = if (modelId != null && sharedTransitionScope != null && animatedVisibilityScope != null) {
        with(sharedTransitionScope) {
            Modifier.sharedBounds(
                sharedContentState = rememberSharedContentState(key = "model_image_$modelId"),
                animatedVisibilityScope = animatedVisibilityScope,
            )
        }
    } else {
        Modifier
    }
    OutlinedCard(
        shape = MaterialTheme.shapes.medium,
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = modifier
            .then(sharedModifier)
            .fillMaxWidth()
            .clip(MaterialTheme.shapes.medium)
            .combinedClickable(
                onClick = onClick,
                onLongClick = onLongClick,
                onDoubleClick = onDoubleClick,
            ),
    ) {
        Box(
            modifier = Modifier.wrapContentSize()
        ) {
            ModelCardContent(
                imageUrl = imageUrl,
                name = name,
                type = type,
                isNsfw = isNsfw,
                showNsfw = showNsfw,
                blurStrength = blurStrength,
                isBlacklisted = isBlacklisted,
                shouldShowMedia = shouldShowMedia,
                blurHash = blurHash,
            )

            // Favorite badge overlay
            if (isFavorite) {
                Box(
                    modifier = Modifier
                        .padding(4.dp)
                        .size(32.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primary)
                        .align(Alignment.TopStart),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Favorite,
                        contentDescription = "Favorite",
                        tint = MaterialTheme.colorScheme.onPrimary,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }

            creatorImage?.let { image ->
                LoadingImage(
                    image,
                    contentScale = ContentScale.FillBounds,
                    modifier = Modifier
                        .padding(4.dp)
                        .size(32.dp)
                        .clip(CircleShape)
                        .align(Alignment.TopEnd)
                )
            }
        }
    }
}

@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
private fun ModelCardContent(
    imageUrl: String,
    name: String,
    type: String,
    isNsfw: Boolean,
    showNsfw: Boolean,
    blurStrength: Dp,
    isBlacklisted: Boolean,
    shouldShowMedia: Boolean,
    blurHash: String? = null,
) {
    Column {
        if (isBlacklisted) {
            Box(
                Modifier
                    .aspectRatio(1f)
                    .background(Color.Black)
            )
        } else {
            if (imageUrl.endsWith("mp4") && shouldShowMedia) {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .fillMaxWidth()
                        .aspectRatio(1f)
                        .background(Color.Black)
                        .then(
                            if (!showNsfw && isNsfw) {
                                Modifier.blur(blurStrength)
                            } else {
                                Modifier
                            }
                        ),
                ) {
                    VideoPreviewComposable(
                        url = imageUrl,
                        frameCount = 5,
                        contentScale = ContentScale.Crop
                    )
                    Row(
                        verticalAlignment = Alignment.Bottom,
                        horizontalArrangement = Arrangement.SpaceEvenly,
                        modifier = Modifier.matchParentSize()
                    ) {
                        Text("Click to Play")
                        Icon(Icons.Default.PlayArrow, null)
                    }
                }
            } else {
                LoadingImage(
                    imageUrl = imageUrl,
                    isNsfw = isNsfw,
                    name = name,
                    hash = blurHash,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .fillMaxWidth()
                        .aspectRatio(1f)
                        .then(
                            if (!showNsfw && isNsfw) {
                                Modifier.blur(blurStrength)
                            } else {
                                Modifier
                            }
                        ),
                )
            }
        }

        ModelCardInfo(
            name = name,
            type = type,
            isNsfw = isNsfw,
        )
    }
}

@Composable
private fun ModelCardInfo(
    name: String,
    type: String,
    isNsfw: Boolean
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(2.dp),
        modifier = Modifier.padding(4.dp)
    ) {
        Text(
            text = name,
            style = MaterialTheme.typography.titleSmall,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )

        Row {
            Text(
                text = type,
                style = MaterialTheme.typography.labelSmall,
                modifier = Modifier
                    .background(
                        color = MaterialTheme.colorScheme.surfaceVariant,
                        shape = MaterialTheme.shapes.medium,
                    )
                    .padding(horizontal = 6.dp, vertical = 2.dp),
            )

            if (isNsfw) {
                Text(
                    text = "NSFW",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onError,
                    modifier = Modifier
                        .background(
                            color = MaterialTheme.colorScheme.error,
                            shape = MaterialTheme.shapes.medium,
                        )
                        .padding(horizontal = 4.dp, vertical = 2.dp),
                )
            }
        }
    }
}

/**
 * Returns a CDN URL resized to the given [width].
 * CivitAI CDN format: .../xG1nkqKTMzGDvpLrqFT7WA/{uuid}/width={size}/{filename}
 */
fun ModelImage.thumbnailUrl(width: Int = 450): String {
    if (!url.contains("image.civitai.com")) return url
    val parts = url.split("/").toMutableList()
    val widthIdx = parts.indexOfFirst { it.startsWith("width=") }
    if (widthIdx != -1) {
        parts[widthIdx] = "width=$width"
    } else if (parts.size > 5) {
        parts.add(5, "width=$width")
    }
    return parts.joinToString("/")
}