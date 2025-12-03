package com.programmersbox.common.components

import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.layout.ContentScale
import com.brys.compose.blurhash.BlurHashImage
import com.programmersbox.resources.Res
import com.programmersbox.resources.civitai_logo
import io.kamel.image.KamelImage
import io.kamel.image.asyncPainterResource
import org.jetbrains.compose.resources.ExperimentalResourceApi
import org.jetbrains.compose.resources.painterResource

@OptIn(ExperimentalResourceApi::class)
@Composable
fun LoadingImage(
    imageUrl: String,
    modifier: Modifier = Modifier,
    isNsfw: Boolean = false,
    hash: String? = null,
    name: String = "",
) {
    KamelImage(
        resource = { asyncPainterResource(imageUrl) },
        onLoading = {
            hash?.let { blurHash ->
                BlurHashImage(
                    hash = blurHash,
                    contentDescription = name,
                    modifier = Modifier.fillMaxSize()
                )
            }
            CircularProgressIndicator(
                progress = { it },
                modifier = Modifier.align(Alignment.Center)
            )
        },
        onFailure = {
            Image(
                painter = painterResource(Res.drawable.civitai_logo),
                contentDescription = null,
                colorFilter = if (isNsfw)
                    ColorFilter.tint(MaterialTheme.colorScheme.error, blendMode = BlendMode.Hue)
                else null,
                modifier = Modifier.fillMaxSize()
            )
        },
        contentScale = ContentScale.Fit,
        contentDescription = name,
        animationSpec = tween(250),
        modifier = modifier
    )
}