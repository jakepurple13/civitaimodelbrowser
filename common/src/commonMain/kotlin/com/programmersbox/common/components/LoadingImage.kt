package com.programmersbox.common.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.layout.ContentScale
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
    name: String = "",
) {
    KamelImage(
        resource = { asyncPainterResource(imageUrl) },
        onLoading = {
            CircularProgressIndicator({ it })
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
        modifier = modifier
    )
}