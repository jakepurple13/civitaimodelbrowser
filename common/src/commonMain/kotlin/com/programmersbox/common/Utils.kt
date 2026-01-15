package com.programmersbox.common

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.animateOffsetAsState
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.gestures.TransformableState
import androidx.compose.foundation.gestures.transformable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyGridState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.FabPosition
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.ScaffoldDefaults
import androidx.compose.material3.contentColorFor
import androidx.compose.material3.windowsizeclass.WindowWidthSizeClass
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.navigationevent.DirectNavigationEventInput
import androidx.navigationevent.compose.LocalNavigationEventDispatcherOwner

object ComposableUtils {
    private const val IMAGE_WIDTH_PX = 360 * 1.5f
    private const val IMAGE_HEIGHT_PX = 480 * 1.5f
    val IMAGE_WIDTH @Composable get() = with(LocalDensity.current) { IMAGE_WIDTH_PX.toDp() }
    val IMAGE_HEIGHT @Composable get() = with(LocalDensity.current) { IMAGE_HEIGHT_PX.toDp() }
}

@Composable
fun adaptiveGridCell(
    minSize: Dp = ComposableUtils.IMAGE_WIDTH,
    minCount: Int = 2,
    maxCount: Int = Int.MAX_VALUE,
): GridCells = CustomAdaptive(
    minSize = minSize,
    minCount = minCount,
    maxCount = maxCount,
)

class CustomAdaptive(
    private val minSize: Dp,
    private val minCount: Int = 1,
    private val maxCount: Int = Int.MAX_VALUE,
) : GridCells {
    init {
        require(minSize > 0.dp)
    }

    override fun Density.calculateCrossAxisCellSizes(
        availableSize: Int,
        spacing: Int,
    ): List<Int> {
        val count = maxOf((availableSize + spacing) / (minSize.roundToPx() + spacing), 1) + 1
        return calculateCellsCrossAxisSizeImpl(
            gridSize = availableSize,
            slotCount = count.coerceIn(minCount, maxCount),
            spacing = spacing
        )
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
    val (x, y) = if (canOffset) animateOffsetAsState(
        sroState.offset,
        label = ""
    ).value else Offset.Zero
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

fun Modifier.ifTrue(isTrue: Boolean, modifierBlock: Modifier.() -> Modifier) =
    if (isTrue) this.modifierBlock()
    else this

@Composable
fun BackButton() {
    BackAction(Icons.AutoMirrored.Filled.ArrowBack)
}

@Composable
fun CloseButton() {
    BackAction(Icons.Default.Close)
}

@Composable
private fun BackAction(
    icon: ImageVector,
    modifier: Modifier = Modifier,
) {
    val navEvent = LocalNavigationEventDispatcherOwner.current?.navigationEventDispatcher

    val navInput = remember { DirectNavigationEventInput() }

    DisposableEffect(Unit) {
        navEvent?.addInput(navInput)
        onDispose { navEvent?.removeInput(navInput) }
    }

    IconButton(
        onClick = { navInput.backCompleted() },
        modifier = modifier
    ) { Icon(icon, null) }
}

enum class CivitSort(val value: String, val visualName: String = value) {
    Newest("Newest"),
    HighestRated("Highest Rated"),
    MostDownloaded("Most Downloaded", "Most Popular")
}

val LocalWindowClassSize = staticCompositionLocalOf { WindowWidthSizeClass.Compact }

@Composable
fun WindowedScaffold(
    modifier: Modifier = Modifier,
    windowClassSize: WindowWidthSizeClass = LocalWindowClassSize.current,
    topBar: @Composable () -> Unit = {},
    bottomBar: @Composable () -> Unit = {},
    rail: @Composable () -> Unit = {},
    snackbarHost: @Composable () -> Unit = {},
    floatingActionButton: @Composable () -> Unit = {},
    floatingActionButtonPosition: FabPosition = FabPosition.End,
    containerColor: Color = MaterialTheme.colorScheme.background,
    contentColor: Color = contentColorFor(containerColor),
    contentWindowInsets: WindowInsets = ScaffoldDefaults.contentWindowInsets,
    content: @Composable (PaddingValues) -> Unit,
) {
    val shouldShowRail = windowClassSize == WindowWidthSizeClass.Medium
            || windowClassSize == WindowWidthSizeClass.Expanded

    Row {
        if (shouldShowRail) rail()
        Scaffold(
            modifier = modifier,
            topBar = topBar,
            bottomBar = {
                if (!shouldShowRail) {
                    bottomBar()
                }
            },
            floatingActionButton = floatingActionButton,
            floatingActionButtonPosition = floatingActionButtonPosition,
            containerColor = containerColor,
            contentColor = contentColor,
            contentWindowInsets = contentWindowInsets,
            snackbarHost = snackbarHost,
            content = content,
        )
    }
}