package com.programmersbox.common.presentation.components

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.kyant.backdrop.Backdrop
import com.kyant.backdrop.backdrops.LayerBackdrop
import com.kyant.backdrop.backdrops.layerBackdrop
import com.kyant.backdrop.backdrops.rememberLayerBackdrop
import com.kyant.backdrop.drawBackdrop
import com.kyant.backdrop.effects.blur
import com.kyant.backdrop.effects.lens
import com.kyant.backdrop.effects.vibrancy
import com.programmersbox.common.DataStore
import dev.chrisbanes.haze.HazeEffectScope
import dev.chrisbanes.haze.HazeState
import dev.chrisbanes.haze.HazeStyle
import dev.chrisbanes.haze.LocalHazeStyle
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

    val hazeState = rememberHazeState(showBlur)
    val hazeStyle = LocalHazeStyle.current

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
    hazeScope = hazeScope
)

fun Modifier.setBlurKind(
    blurKind: BlurKind,
    hazeState: HazeState,
    hazeStyle: HazeStyle,
    backdrop: Backdrop,
    backgroundColor: Color,
    hazeScope: HazeEffectScope.() -> Unit
) = when (blurKind) {
    BlurKind.Haze -> hazeEffect(hazeState, hazeStyle, hazeScope)
    BlurKind.LiquidGlass -> drawBackdrop(
        backdrop = backdrop,
        shape = { RoundedCornerShape(1.dp) },
        effects = {
            vibrancy()
            blur(4f.dp.toPx())
            lens(16f.dp.toPx(), 32f.dp.toPx())
        },
        onDrawSurface = { drawRect(backgroundColor.copy(alpha = 0.5f)) }
    )
}

fun Modifier.setBlurKindSource(blurKindState: BlurKindState) =
    hazeSource(state = blurKindState.hazeState.hazeState)
        .layerBackdrop(blurKindState.liquidState.backdrop)

@Serializable
enum class BlurKind {
    Haze,
    LiquidGlass
}