package com.programmersbox.common

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.animateOffsetAsState
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.gestures.TransformableState
import androidx.compose.foundation.gestures.rememberTransformableState
import androidx.compose.foundation.gestures.transformable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyGridState
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

object ComposableUtils {
    private const val IMAGE_WIDTH_PX = 360 * 1.5f
    private const val IMAGE_HEIGHT_PX = 480 * 1.5f
    val IMAGE_WIDTH @Composable get() = with(LocalDensity.current) { IMAGE_WIDTH_PX.toDp() }
    val IMAGE_HEIGHT @Composable get() = with(LocalDensity.current) { IMAGE_HEIGHT_PX.toDp() }
}

@Composable
fun adaptiveGridCell(
    minSize: Dp = ComposableUtils.IMAGE_WIDTH,
    minCount: Int = 1,
): GridCells = CustomAdaptive(
    minSize = minSize,
    minCount = minCount
)

class CustomAdaptive(
    private val minSize: Dp,
    private val minCount: Int = 1,
) : GridCells {
    init {
        require(minSize > 0.dp)
    }

    override fun Density.calculateCrossAxisCellSizes(
        availableSize: Int,
        spacing: Int,
    ): List<Int> {
        val count = maxOf((availableSize + spacing) / (minSize.roundToPx() + spacing), 1) + 1
        return calculateCellsCrossAxisSizeImpl(availableSize, count.coerceAtLeast(minCount), spacing)
    }

    override fun hashCode(): Int {
        return minSize.hashCode()
    }

    override fun equals(other: Any?): Boolean {
        return other is CustomAdaptive && minSize == other.minSize
    }
}

private fun calculateCellsCrossAxisSizeImpl(
    gridSize: Int,
    slotCount: Int,
    spacing: Int,
): List<Int> {
    val gridSizeWithoutSpacing = gridSize - spacing * (slotCount - 1)
    val slotSize = gridSizeWithoutSpacing / slotCount
    val remainingPixels = gridSizeWithoutSpacing % slotCount
    return List(slotCount) {
        slotSize + if (it < remainingPixels) 1 else 0
    }
}

@Composable
fun LazyGridState.isScrollingUp(): Boolean {
    var previousIndex by remember(this) { mutableIntStateOf(firstVisibleItemIndex) }
    var previousScrollOffset by remember(this) { mutableIntStateOf(firstVisibleItemScrollOffset) }
    return remember(this) {
        derivedStateOf {
            if (previousIndex != firstVisibleItemIndex) {
                previousIndex > firstVisibleItemIndex
            } else {
                previousScrollOffset >= firstVisibleItemScrollOffset
            }.also {
                previousIndex = firstVisibleItemIndex
                previousScrollOffset = firstVisibleItemScrollOffset
            }
        }
    }.value
}

interface ScaleRotateOffsetResetScope {

    fun reset()


    @OptIn(ExperimentalFoundationApi::class)
    fun Modifier.scaleRotateOffsetReset(
        canScale: Boolean = true,
        canRotate: Boolean = true,
        canOffset: Boolean = true,
        onClick: () -> Unit = {},
        onLongClick: () -> Unit = {},
    ): Modifier = this.composed {
        var scale by remember { mutableFloatStateOf(1f) }
        var rotation by remember { mutableFloatStateOf(0f) }
        var offset by remember { mutableStateOf(Offset.Zero) }

        val state = rememberTransformableState { zoomChange, offsetChange, rotationChange ->
            if (canScale) scale *= zoomChange
            if (canRotate) rotation += rotationChange
            if (canOffset) offset += offsetChange
        }

        val animScale = animateFloatAsState(scale, label = "").value
        val (x, y) = animateOffsetAsState(offset, label = "").value
        graphicsLayer(
            scaleX = animScale,
            scaleY = animScale,
            rotationZ = animateFloatAsState(rotation, label = "").value,
            translationX = x,
            translationY = y
        )
            // add transformable to listen to multitouch transformation events
            // after offset
            .transformable(state = state)
            .combinedClickable(
                onClick = onClick,
                onDoubleClick = {
                    scale = 1f
                    rotation = 0f
                    offset = Offset.Zero
                },
                onLongClick = onLongClick,
                indication = null,
                interactionSource = remember { MutableInteractionSource() }
            )
    }
}

@Composable
fun rememberSROState(): SROState = remember { SROState() }

class SROState {
    var scale by mutableFloatStateOf(1f)
    var rotation by mutableFloatStateOf(0f)
    var offset by mutableStateOf(Offset.Zero)
    val state = TransformableState { zoomChange, panChange, rotationChange ->
        scale *= zoomChange
        rotation += rotationChange
        offset += panChange
    }

    fun reset() {
        scale = 1f
        rotation = 0f
        offset = Offset.Zero
    }
}

@Composable
@OptIn(ExperimentalFoundationApi::class)
fun Modifier.scaleRotateOffsetReset(
    sroState: SROState = rememberSROState(),
    canScale: Boolean = true,
    canRotate: Boolean = true,
    canOffset: Boolean = true,
    onClick: () -> Unit = {},
    onLongClick: () -> Unit = {},
): Modifier = this.composed {
    val animScale = if (canScale) animateFloatAsState(sroState.scale, label = "").value else 1f
    val (x, y) = if (canOffset) animateOffsetAsState(sroState.offset, label = "").value else Offset.Zero
    val rotation = if (canRotate) animateFloatAsState(sroState.rotation, label = "").value else 0f
    graphicsLayer(
        scaleX = animScale,
        scaleY = animScale,
        rotationZ = rotation,
        translationX = x,
        translationY = y
    )
        // add transformable to listen to multitouch transformation events
        // after offset
        .transformable(state = sroState.state)
        .combinedClickable(
            onClick = onClick,
            onDoubleClick = { sroState.reset() },
            onLongClick = onLongClick,
            indication = null,
            interactionSource = remember { MutableInteractionSource() }
        )
}

fun Modifier.ifTrue(isTrue: Boolean, modifierBlock: Modifier.() -> Modifier) = if (isTrue) this.modifierBlock()
else this