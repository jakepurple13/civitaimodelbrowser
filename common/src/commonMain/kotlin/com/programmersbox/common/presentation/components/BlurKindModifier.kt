package com.programmersbox.common.presentation.components

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.dp
import com.kyant.backdrop.Backdrop
import com.kyant.backdrop.backdrops.LayerBackdrop
import com.kyant.backdrop.backdrops.layerBackdrop
import com.kyant.backdrop.backdrops.rememberLayerBackdrop
import com.kyant.backdrop.drawBackdrop
import com.kyant.backdrop.effects.blur
import com.kyant.backdrop.effects.lens
import com.kyant.backdrop.effects.vibrancy
import com.kyant.backdrop.highlight.Highlight
import com.programmersbox.common.DataStore
import dev.chrisbanes.haze.HazeEffectScope
import dev.chrisbanes.haze.HazeState
import dev.chrisbanes.haze.HazeStyle
import dev.chrisbanes.haze.hazeEffect
import dev.chrisbanes.haze.hazeSource
import dev.chrisbanes.haze.rememberHazeState
import kotlinx.serialization.Serializable
import org.koin.compose.koinInject

@Composable
fun rememberBlurKindState(
    dataStore: DataStore = koinInject(),
    backgroundColor: Color = MaterialTheme.colorScheme.surface,
): BlurKindState {
    val showBlur by dataStore.rememberShowBlur()
    val useProgressive by dataStore.rememberUseProgressive()

    val blurKind by dataStore.rememberBlurKind()
    val blurType by dataStore.rememberBlurType()

    val hazeState = rememberHazeState(showBlur)
    val hazeStyle = blurType.toHazeStyle()

    val backdrop = rememberLayerBackdrop {
        drawRect(backgroundColor)
        drawContent()
    }

    return remember(
        blurKind,
        hazeState,
        hazeStyle,
        backdrop,
        showBlur,
        useProgressive,
        backgroundColor
    ) {
        BlurKindState(
            blurKind = blurKind,
            showBlur = showBlur,
            hazeState = BlurKindHazeState(
                hazeState = hazeState,
                hazeStyle = hazeStyle,
                useProgressive = useProgressive,
            ),
            liquidState = BlurKindLiquidState(
                backdrop = backdrop,
                backgroundColor = backgroundColor
            )
        )
    }
}

@Stable
class BlurKindState(
    val blurKind: BlurKind,
    val showBlur: Boolean,
    val hazeState: BlurKindHazeState,
    val liquidState: BlurKindLiquidState,
)

@Stable
class BlurKindHazeState(
    val hazeState: HazeState,
    val hazeStyle: HazeStyle,
    val useProgressive: Boolean,
)

@Stable
class BlurKindLiquidState(
    val backdrop: LayerBackdrop,
    val backgroundColor: Color
)

fun Modifier.setBlurKind(
    blurKindState: BlurKindState,
    hazeScope: HazeEffectScope.() -> Unit = {}
) = setBlurKind(
    blurKind = blurKindState.blurKind,
    hazeState = blurKindState.hazeState.hazeState,
    hazeStyle = blurKindState.hazeState.hazeStyle,
    backdrop = blurKindState.liquidState.backdrop,
    backgroundColor = blurKindState.liquidState.backgroundColor,
    showBlur = blurKindState.showBlur,
    hazeScope = hazeScope
)

private fun Modifier.setBlurKind(
    blurKind: BlurKind,
    hazeState: HazeState,
    hazeStyle: HazeStyle,
    backdrop: Backdrop,
    backgroundColor: Color,
    showBlur: Boolean,
    hazeScope: HazeEffectScope.() -> Unit
) = when (blurKind) {
    BlurKind.Haze if showBlur -> hazeEffect(hazeState, hazeStyle, hazeScope)
    BlurKind.LiquidGlass if showBlur -> drawBackdrop(
        backdrop = backdrop,
        shape = { RoundedCornerShape(1.dp) },
        effects = {
            vibrancy()
            blur(1f.dp.toPx())
            lens(
                refractionHeight = 12.dp.toPx(),
                refractionAmount = 32.dp.toPx(),
                depthEffect = true,
                chromaticAberration = true
            )
        },
        onDrawSurface = { drawRect(backgroundColor.copy(alpha = 0.5f)) },
        highlight = { Highlight.Ambient }
    )

    else -> this
}

fun Modifier.setBlurKindSource(blurKindState: BlurKindState) = when (blurKindState.blurKind) {
    BlurKind.Haze if blurKindState.showBlur -> hazeSource(blurKindState.hazeState.hazeState)
    BlurKind.LiquidGlass if blurKindState.showBlur -> layerBackdrop(blurKindState.liquidState.backdrop)
    else -> this
}

@Serializable
enum class BlurKind {
    Haze,
    LiquidGlass
}

fun Modifier.floatingActionButtonBlurKind(
    blurKindState: BlurKindState,
    shape: Shape
) = when (blurKindState.blurKind) {
    BlurKind.Haze if blurKindState.showBlur -> this
    BlurKind.LiquidGlass if blurKindState.showBlur -> drawBackdrop(
        backdrop = blurKindState.liquidState.backdrop,
        shape = { shape },
        effects = {
            vibrancy()
            blur(1f.dp.toPx())
            lens(
                refractionHeight = 16.dp.toPx(),
                refractionAmount = 38.dp.toPx(),
                depthEffect = true,
                chromaticAberration = true
            )
        },
        onDrawSurface = {
            drawRect(
                blurKindState
                    .liquidState
                    .backgroundColor
                    .copy(alpha = 0.5f)
            )
        },
        highlight = if (blurKindState.blurKind == BlurKind.LiquidGlass) {
            { Highlight.Default }
        } else null,
        shadow = null
    )

    else -> this
}