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

    val liquidGlassBlurAmount by dataStore.rememberLiquidGlassBlurAmount()
    val liquidGlassRefractionHeight by dataStore.rememberLiquidGlassRefractionHeight()
    val liquidGlassRefractionAmount by dataStore.rememberLiquidGlassRefractionAmount()
    val liquidGlassDepthEffect by dataStore.rememberLiquidGlassDepthEffect()
    val liquidGlassChromaticAberration by dataStore.rememberLiquidGlassChromaticAberration()

    return remember(
        blurKind,
        hazeState,
        hazeStyle,
        backdrop,
        showBlur,
        useProgressive,
        backgroundColor,
        liquidGlassBlurAmount,
        liquidGlassRefractionHeight,
        liquidGlassRefractionAmount,
        liquidGlassDepthEffect,
        liquidGlassChromaticAberration
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
                backgroundColor = backgroundColor,
                blurAmount = liquidGlassBlurAmount,
                refractionHeight = liquidGlassRefractionHeight,
                refractionAmount = liquidGlassRefractionAmount,
                depthEffect = liquidGlassDepthEffect,
                chromaticAberration = liquidGlassChromaticAberration,
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
    val backgroundColor: Color,
    val blurAmount: Float,
    val refractionHeight: Float,
    val refractionAmount: Float,
    val depthEffect: Boolean,
    val chromaticAberration: Boolean,
)

fun Modifier.setBlurKind(
    blurKindState: BlurKindState,
    liquidGlassShape: () -> Shape = { RoundedCornerShape(1.dp) },
    hazeScope: HazeEffectScope.() -> Unit = {}
) = when (blurKindState.blurKind) {
    BlurKind.Haze if blurKindState.showBlur -> hazeEffect(
        state = blurKindState.hazeState.hazeState,
        style = blurKindState.hazeState.hazeStyle,
        block = hazeScope
    )

    BlurKind.LiquidGlass if blurKindState.showBlur -> drawBackdrop(
        backdrop = blurKindState.liquidState.backdrop,
        shape = liquidGlassShape,
        effects = {
            vibrancy()
            blur(blurKindState.liquidState.blurAmount.dp.toPx())
            lens(
                refractionHeight = blurKindState.liquidState.refractionHeight.dp.toPx(),
                refractionAmount = blurKindState.liquidState.refractionAmount.dp.toPx(),
                depthEffect = blurKindState.liquidState.depthEffect,
                chromaticAberration = blurKindState.liquidState.chromaticAberration
            )
        },
        onDrawSurface = { drawRect(blurKindState.liquidState.backgroundColor.copy(alpha = 0.5f)) },
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