package com.programmersbox.common.presentation.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.MutableTransitionState
import androidx.compose.animation.core.animate
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.draggable
import androidx.compose.foundation.gestures.rememberDraggableState
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.calculateEndPadding
import androidx.compose.foundation.layout.calculateStartPadding
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.layout.LazyLayout
import androidx.compose.foundation.lazy.layout.LazyLayoutItemProvider
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicText
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Warning
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.ProvidableCompositionLocal
import androidx.compose.runtime.Stable
import androidx.compose.runtime.State
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.max
import androidx.compose.ui.window.Popup
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.flatMapMerge
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.mapNotNull
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlin.jvm.JvmName
import kotlin.math.absoluteValue
import kotlin.math.max
import kotlin.math.min
import kotlin.time.Duration
import kotlin.time.Duration.Companion.milliseconds

internal fun colorsOf(type: ToastType, darkTheme: Boolean): ToastColors {
    val colors = if (darkTheme) DarkToastColors else LightToastColors
    return colors[type]!!
}

internal val LightToastColors = mapOf(
    ToastType.Normal to ToastColors(
        background = Color.White,
        content = Color(0xff171717),
        border = Color(0xffededed),
    ),
    ToastType.Success to ToastColors(
        background = Color(0xffecfdf3),
        content = Color(0xff008a2e),
        border = Color(0xffd3fde5),
    ),
    ToastType.Info to ToastColors(
        background = Color(0xfff0f8ff),
        content = Color(0xfff0973dc),
        border = Color(0xffd3e0fd),
    ),
    ToastType.Warning to ToastColors(
        background = Color(0xfffffcf0),
        content = Color(0xffdc7609),
        border = Color(0xfffdf5d3),
    ),
    ToastType.Error to ToastColors(
        background = Color(0xfffff0f0),
        content = Color(0xffe60000),
        border = Color(0xffffe0e1),
    ),
)

internal val DarkToastColors = mapOf(
    ToastType.Normal to ToastColors(
        background = Color.Black,
        content = Color(0xfffcfcfc),
        border = Color(0xff333333),
    ),
    ToastType.Success to ToastColors(
        background = Color(0xff001f0f),
        content = Color(0xff59f3a6),
        border = Color(0xff003d1c),
    ),
    ToastType.Info to ToastColors(
        background = Color(0xff000d1f),
        content = Color(0xff5896f3),
        border = Color(0xff00113d),
    ),
    ToastType.Warning to ToastColors(
        background = Color(0xff1d1f00),
        content = Color(0xfff3cf58),
        border = Color(0xff3d3d00),
    ),
    ToastType.Error to ToastColors(
        background = Color(0xff2d0607),
        content = Color(0xffff9ea1),
        border = Color(0xff4d0408),
    ),
)

internal class ToastColors(
    val background: Color,
    val content: Color,
    val border: Color,
)

/**
 * The default toast close button.
 */
@Composable
fun ToastCloseButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    border: BorderStroke = LocalToastBorderStroke.current,
    background: Brush = LocalToastBackground.current,
    tint: Color = LocalToastContentColor.current,
    size: Dp = CloseButtonSize,
    shape: Shape = CircleShape,
    offset: DpOffset = DpOffset(-(8).dp, -(8).dp),
    contentDescription: String? = null,
) {
    Box(
        modifier = modifier
            .size(size)
            .offset(offset.x, offset.y)
            .clip(CircleShape)
            .background(background)
            .border(
                width = border.width,
                brush = border.brush,
                shape = shape,
            )
            .clickable { onClick() }
            .padding(4.dp)
            .testTag("CloseButton"),
        contentAlignment = Alignment.Center,
    ) {
        Image(
            imageVector = Icons.Default.Close,
            contentDescription = contentDescription,
            colorFilter = ColorFilter.tint(tint),
        )
    }
}

@Stable
internal class ItemHeightProvider {
    private val itemHeights = mutableStateOf<Map<Int, Int>>(emptyMap())

    fun updateItemHeights(heights: Map<Int, Int>) {
        itemHeights.value = heights
    }

    fun get(layoutIndex: Int): Int {
        return itemHeights.value[layoutIndex] ?: 0
    }

    fun listen(layoutIndex: Int): Flow<Int> {
        return snapshotFlow { itemHeights.value }
            .mapNotNull { itemHeights.value[layoutIndex] }
            .distinctUntilChanged()
    }
}

@Composable
internal fun rememberLazyToasterBoxState(
    maxVisibleToasts: Int,
    itemCountProvider: () -> Int,
    key: (index: Int) -> Any,
    indexOfKey: (key: Any) -> Int,
    isItemDismissed: (index: Int) -> Boolean,
): LazyToasterBoxState {
    return remember {
        LazyToasterBoxState(
            maxVisibleToasts = maxVisibleToasts,
            itemCountProvider = itemCountProvider,
            key = key,
            indexOfKey = indexOfKey,
            isItemDismissed = isItemDismissed,
        )
    }.also {
        it.maxVisibleToasts = maxVisibleToasts
    }
}

/**
 * A lazy layout that stacks all children at the top or bottom side according to the [alignment].
 *
 * The [alignment] does not really to be used, only for determining the top/bottom.
 */
@OptIn(ExperimentalFoundationApi::class)
@Composable
internal fun LazyToasterBox(
    state: LazyToasterBoxState,
    expanded: Boolean,
    itemHeightProvider: ItemHeightProvider,
    toastTransformHelper: ToastTransformHelper,
    modifier: Modifier = Modifier,
    alignment: Alignment = Alignment.BottomCenter,
    contentPadding: PaddingValues = PaddingValues(0.dp),
    itemContent: @Composable (index: Int) -> Unit,
) {
    val itemProvider = remember(state) {
        ToasterItemProvider(
            state = state,
            itemContent = itemContent,
        )
    }

    LazyLayout(
        itemProvider = { itemProvider },
        modifier = modifier,
    ) { constraints ->
        val topPadding = contentPadding.calculateTopPadding().roundToPx()
        val bottomPadding = contentPadding.calculateBottomPadding().roundToPx()
        val startPadding = contentPadding.calculateStartPadding(layoutDirection).roundToPx()
        val endPadding = contentPadding.calculateEndPadding(layoutDirection).roundToPx()

        val updatedConstrains = constraints.copy(
            maxHeight = constraints.maxHeight - topPadding - bottomPadding,
            maxWidth = constraints.maxWidth - startPadding - endPadding,
        )

        val visibleItemIndices = state.visibleItemIndices()
        if (visibleItemIndices.isEmpty()) {
            return@LazyLayout layout(startPadding + endPadding, topPadding + bottomPadding) {}
        }

        val placeables = visibleItemIndices.flatMap {
            if (!state.isItemDismissed(it)) {
                measure(it, updatedConstrains)
            } else {
                emptyList()
            }
        }
        if (placeables.isEmpty()) {
            return@LazyLayout layout(startPadding + endPadding, topPadding + bottomPadding) {}
        }

        var maxItemWidth = 0
        var totalItemHeight = 0
        val layoutHeightMap = mutableMapOf<Int, Int>()
        for (i in placeables.indices) {
            val placeable = placeables[i]
            maxItemWidth = max(maxItemWidth, placeable.width)
            totalItemHeight += placeable.height
            val layoutIndex = placeables.lastIndex - i
            layoutHeightMap[layoutIndex] = placeable.height
        }

        val layoutWidth = maxItemWidth + startPadding + endPadding

        val stackFromBottom = alignment.isBottomAlign()

        itemHeightProvider.updateItemHeights(layoutHeightMap)

        val lastItemTranY = toastTransformHelper.calcTranslationY(
            itemHeightProvider = itemHeightProvider,
            isBottomAlign = stackFromBottom,
            expanded = expanded,
            layoutIndex = placeables.lastIndex,
        )
        val layoutHeight = min(
            constraints.maxHeight,
            placeables.last().height + lastItemTranY.absoluteValue.toInt(),
        )

        layout(layoutWidth, layoutHeight) {
            for (i in placeables.indices) {
                val placeable = placeables[i]
                val y = if (stackFromBottom) {
                    layoutHeight - placeable.height - bottomPadding
                } else {
                    topPadding
                }
                placeable.place(
                    x = startPadding,
                    y = y
                )
            }
        }
    }
}

@ExperimentalFoundationApi
private class ToasterItemProvider(
    private val state: LazyToasterBoxState,
    private val itemContent: @Composable (index: Int) -> Unit,
) : LazyLayoutItemProvider {
    override val itemCount: Int get() = state.itemCount()

    @Composable
    override fun Item(index: Int, key: Any) {
        itemContent(index)
    }

    override fun getKey(index: Int): Any {
        return state.keyForIndex(index)
    }

    override fun getContentType(index: Int): Any {
        return 0
    }

    override fun getIndex(key: Any): Int {
        return state.indexOfKey(0)
    }
}

@Stable
internal class LazyToasterBoxState(
    maxVisibleToasts: Int,
    private val itemCountProvider: () -> Int,
    private val key: (index: Int) -> Any,
    private val indexOfKey: (key: Any) -> Int,
    private val isItemDismissed: (index: Int) -> Boolean,
) {
    var maxVisibleToasts by mutableIntStateOf(maxVisibleToasts)

    fun itemCount() = itemCountProvider()

    fun visibleItemIndices(): List<Int> {
        val maxCount = maxVisibleToasts + 1
        val visibleIndices = mutableListOf<Int>()
        val lastIndex = itemCount() - 1
        for (i in lastIndex downTo 0) {
            if (visibleIndices.size == maxCount) break
            if (!isItemDismissed(i)) {
                visibleIndices.add(i)
            }
        }
        return visibleIndices.reversed()
    }

    fun keyForIndex(index: Int) = key(index)

    fun indexOfKey(key: Any) = indexOfKey.invoke(key)

    fun isItemDismissed(index: Int): Boolean {
        return isItemDismissed.invoke(index)
    }
}

/**
 * Composition local to get the content color within the toast slots.
 */
val LocalToastContentColor = compositionLocalOf { Color.Black }

/**
 * Composition local to get the border stroke within the toast slots.
 */
val LocalToastBorderStroke = compositionLocalOf { BorderStroke(1.dp, Color.LightGray) }

/**
 * Composition local to get the background brush within the toast slots.
 */
val LocalToastBackground: ProvidableCompositionLocal<Brush> = compositionLocalOf {
    SolidColor(Color.White)
}

internal expect fun currentNanoTime(): Long

internal expect val CloseButtonSize: Dp

@Composable
internal expect fun ToasterPopup(
    alignment: Alignment,
    modifier: Modifier = Modifier,
    offset: IntOffset = IntOffset.Zero,
    content: @Composable () -> Unit,
)

/**
 * Class representing a toast to be displayed by the Toaster.
 *
 * @param message The message content of the toast. [toString] will be called for displaying.
 * @param id Id for the toast.
 * @param icon Optional icon for the toast. It can be any type, to display it, a custom
 * icon slot must be set for the [Toaster].
 * @param action Optional action associated with the toast. If any action than [TextToastAction]
 * is set, a custom action slot must be set for the [Toaster].
 * @param type Type of the toast, specified by [ToastType].
 * @param duration Duration for which the toast should be displayed.
 */
@Immutable
class Toast(
    val message: Any,
    val id: Any = currentNanoTime(),
    val icon: Any? = null,
    val action: Any? = null,
    val type: ToastType = ToastType.Normal,
    val duration: Duration = ToasterDefaults.DurationDefault,
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is Toast) return false

        if (id != other.id) return false
        if (icon != other.icon) return false
        if (message != other.message) return false
        if (action != other.action) return false
        if (type != other.type) return false
        if (duration != other.duration) return false

        return true
    }

    override fun hashCode(): Int {
        var result = id.hashCode()
        result = 31 * result + (icon?.hashCode() ?: 0)
        result = 31 * result + message.hashCode()
        result = 31 * result + (action?.hashCode() ?: 0)
        result = 31 * result + type.hashCode()
        result = 31 * result + duration.hashCode()
        return result
    }
}

/**
 * The width policy for each toast.
 */
@Immutable
class ToastWidthPolicy(
    val min: Dp = Dp.Unspecified,
    val max: Dp = 380.dp,
    val fillMaxWidth: Boolean = true,
)

/**
 * The action that contains a [text] field and a [onClick] callback.
 */
@Immutable
class TextToastAction(
    val text: String,
    val onClick: (toast: Toast) -> Unit,
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is TextToastAction) return false

        if (text != other.text) return false
        if (onClick != other.onClick) return false

        return true
    }

    override fun hashCode(): Int {
        var result = text.hashCode()
        result = 31 * result + onClick.hashCode()
        return result
    }
}

/**
 * Toast types.
 */
enum class ToastType {
    Normal,
    Success,
    Info,
    Warning,
    Error,
}

/**
 * The pause strategy of the toast dismiss timer.
 */
enum class ToastDismissPause {
    /**
     * Never pause the dismiss timer.
     */
    Never,

    /**
     * Pause the dismiss timer when the toast is no longer at the front of the toast stack.
     */
    OnNotFront,

    /**
     * Pause the dismiss timer when the toast is moved out from the visible toast stack.
     */
    OnInvisible,
}

/**
 * Helper class to calculate toast item transforms.
 */
@Stable
internal class ToastTransformHelper(
    private val density: Density,
    private val maxVisibleToasts: Int,
) {
    fun calcScale(layoutIndex: Int): Float {
        val index = layoutIndex.coerceIn(0, maxVisibleToasts)
        return 1f - 0.4f * (index / maxVisibleToasts.toFloat())
    }

    fun calcTranslationY(
        itemHeightProvider: ItemHeightProvider,
        isBottomAlign: Boolean,
        expanded: Boolean,
        layoutIndex: Int,
    ): Float {
        val index = layoutIndex.coerceIn(0, maxVisibleToasts)
        if (index == 0) {
            // No offset for the front item
            return 0f
        }
        val frontItemHeight = itemHeightProvider.get(0)
        val factor = if (isBottomAlign) -1f else 1f
        return if (expanded) {
            val impactOffset = with(density) { ((-16).dp).toPx() }
            var tranY = 0f
            // Just add up the heights of the items
            for (i in 0..<index) {
                tranY += itemHeightProvider.get(0) + impactOffset
            }
            tranY * factor
        } else {
            val maxOffset = with(density) { 12.dp.toPx() }
            val scale = 1f - 0.3f * (index / maxVisibleToasts.toFloat())
            val diff = if (frontItemHeight > 0) {
                // Apply the diff between the current item height and the front item height,
                // or else the current item will get overlapped if they have different heights.
                frontItemHeight - itemHeightProvider.get(index)
            } else {
                0
            }
            (maxOffset * index + diff) * scale * factor
        }
    }
}

/**
 * The toaster, within a [Popup]. It will display toasts from the toaster [state].
 *
 * @param state The state of the toaster, managing the toasts to be displayed.
 * @param modifier The modifier to be applied to the container.
 * @param maxVisibleToasts Maximum number of toasts visible at the same time.
 * @param expanded Whether toasts are expanded vertically instead of stacked.
 * @param swipeable Whether toasts can be dismissed with a swipe down gesture.
 * @param richColors Whether to use rich colors for toasts that have a different type than [ToastType.Normal].
 * @param darkTheme Whether toasts are in dark theme.
 * @param showCloseButton Whether to show a close button on the top-left corner for each toast.
 * @param contentColor Composable function that provides the content color of each toast.
 * @param border Composable function that provides the border stroke of each toast.
 * @param background Composable function that provides the background brush of each toast.
 * @param shape Shape of toasts.
 * @param elevation `elevation` of toast's [shadow] modifier.
 * @param shadowAmbientColor `ambientColor` of toast's [shadow] modifier.
 * @param shadowSpotColor `spotColor` of toast's [shadow] modifier.
 * @param containerPadding Padding for the toaster container.
 * @param contentPadding Padding for the content of each toast.
 * @param widthPolicy The width policy for each toast.
 * @param alignment Alignment of the toaster within the popup.
 * @param offset Offset of the popup.
 * @param enterTransitionDuration Duration of the enter transition for each toast.
 * @param exitTransitionDuration Duration of the exit transition for each toast.
 * @param iconSlot Composable slot for the toast icon.
 * @param messageSlot Composable slot for the toast message.
 * @param actionSlot Composable slot for the toast action.
 * @param closeButton The close button, if set, [showCloseButton] will be ignored.
 * @param toastBox A wrapper for each toast.
 */
@Composable
fun Toaster(
    state: ToasterState,
    modifier: Modifier = Modifier,
    maxVisibleToasts: Int = 3,
    expanded: Boolean = false,
    swipeable: Boolean = true,
    richColors: Boolean = false,
    darkTheme: Boolean = false,
    showCloseButton: Boolean = false,
    contentColor: @Composable (toast: Toast) -> Color = {
        ToasterDefaults.contentColor(it, richColors, darkTheme)
    },
    border: @Composable (toast: Toast) -> BorderStroke = {
        ToasterDefaults.border(it, richColors, darkTheme)
    },
    background: @Composable (toast: Toast) -> Brush = {
        ToasterDefaults.background(it, richColors, darkTheme)
    },
    shape: @Composable (toast: Toast) -> Shape = { ToasterDefaults.Shape },
    elevation: Dp = ToasterDefaults.Elevation,
    shadowAmbientColor: Color = if (darkTheme) {
        ToasterDefaults.DarkShadowAmbientColor
    } else {
        ToasterDefaults.ShadowAmbientColor
    },
    shadowSpotColor: Color = if (darkTheme) {
        ToasterDefaults.DarkShadowSpotColor
    } else {
        ToasterDefaults.ShadowSpotColor
    },
    containerPadding: PaddingValues = PaddingValues(0.dp),
    contentPadding: @Composable (toast: Toast) -> PaddingValues = { PaddingValues(16.dp) },
    widthPolicy: @Composable (toast: Toast) -> ToastWidthPolicy = { ToastWidthPolicy() },
    alignment: Alignment = Alignment.BottomCenter,
    offset: IntOffset = IntOffset.Zero,
    enterTransitionDuration: Int = ToasterDefaults.ENTER_TRANSITION_DURATION,
    exitTransitionDuration: Int = ToasterDefaults.EXIT_TRANSITION_DURATION,
    dismissPause: ToastDismissPause = ToastDismissPause.OnInvisible,
    iconSlot: @Composable (toast: Toast) -> Unit = { ToasterDefaults.iconSlot(it) },
    messageSlot: @Composable (toast: Toast) -> Unit = { ToasterDefaults.messageSlot(it) },
    actionSlot: @Composable (toast: Toast) -> Unit = { ToasterDefaults.actionSlot(it) },
    closeButton: @Composable (BoxScope.(toast: Toast) -> Unit)? = if (showCloseButton) {
        {
            ToastCloseButton(onClick = { state.dismiss(it.id) })
        }
    } else null,
    toastBox: @Composable (toast: Toast, toastContent: @Composable () -> Unit) -> Unit =
        { _, content -> content() }
) {
    require(maxVisibleToasts > 0) { "maxVisibleToasts should be at least 1." }

    if (state.toasts.isEmpty()) return

    ToasterPopup(alignment = alignment, offset = offset) {
        val density = LocalDensity.current

        val lazyToasterBoxState = rememberLazyToasterBoxState(
            maxVisibleToasts = maxVisibleToasts,
            itemCountProvider = { state.toasts.size },
            key = { index -> state.toasts[index].toast.id },
            indexOfKey = { key -> state.toasts.indexOfFirst { it.toast.id == key } },
            isItemDismissed = { index -> state.toasts[index].isDismissed }
        )


        val itemHeightProvider = remember { ItemHeightProvider() }

        val toastTransformHelper = remember(density, maxVisibleToasts) {
            ToastTransformHelper(density = density, maxVisibleToasts = maxVisibleToasts)
        }

        LaunchedEffect(state.toasts) {
            state.dismissingToastsFlow()
                .collect { item ->
                    val visibleItemIndices = lazyToasterBoxState.visibleItemIndices()
                    val index = state.toasts.indexOf(item)
                    if (index !in visibleItemIndices) {
                        // Item dismissed but not currently visible, mark it as
                        // dismissed state and don't render it on UI
                        state.markDismissed(item.toast.id)
                    }
                }
        }

        ApplyToastDismissPause(
            state = state,
            toastDismissPause = dismissPause,
            lazyToasterBoxState = lazyToasterBoxState,
            maxVisibleToasts = maxVisibleToasts,
        )

        LazyToasterBox(
            state = lazyToasterBoxState,
            expanded = expanded,
            itemHeightProvider = itemHeightProvider,
            toastTransformHelper = toastTransformHelper,
            contentPadding = containerPadding,
            alignment = alignment,
            modifier = modifier.testTag("Toaster"),
        ) { index ->
            val item = state.toasts[index]
            val toast = item.toast

            var invisibleItemCount by remember { mutableIntStateOf(0) }

            LaunchedEffect(state.toasts.lastIndex, index) {
                // This finds all dismissing or dismissed item count above this current item,
                // will make our toasts animated after some have dismissed.
                state.invisibleItemsInRangeFlow(
                    start = index + 1,
                    end = state.toasts.lastIndex
                )
                    .collect { invisibleItemCount = it }
            }

            val layoutIndex = state.toasts.lastIndex - index - invisibleItemCount

            val currentBorder = border(toast)
            val currentBackground = background(toast)
            val currentContentColor = contentColor(toast)

            CompositionLocalProvider(
                LocalToastBorderStroke provides currentBorder,
                LocalToastBackground provides currentBackground,
                LocalToastContentColor provides currentContentColor,
            ) {
                toastBox(item.toast) {
                    ToastItem(
                        onRequestDismiss = { state.dismiss(toast.id) },
                        onInvisible = { state.markDismissed(toast.id) },
                        expanded = expanded,
                        layoutIndex = layoutIndex,
                        toast = item.toast,
                        dismissing = item.isDismissing,
                        maxVisibleToasts = maxVisibleToasts,
                        widthPolicy = widthPolicy(toast),
                        swipeable = swipeable,
                        elevation = elevation,
                        shadowAmbientColor = shadowAmbientColor,
                        shadowSpotColor = shadowSpotColor,
                        shape = shape(toast),
                        contentPadding = contentPadding(toast),
                        alignment = alignment,
                        enterTransitionDuration = enterTransitionDuration,
                        exitTransitionDuration = exitTransitionDuration,
                        transformHelper = toastTransformHelper,
                        itemHeightProvider = itemHeightProvider,
                        modifier = Modifier,
                        border = currentBorder,
                        background = currentBackground,
                        iconSlot = iconSlot,
                        messageSlot = messageSlot,
                        actionSlot = actionSlot,
                        closeButton = closeButton,
                    )
                }
            }
        }
    }
}

@Composable
private inline fun ApplyToastDismissPause(
    state: ToasterState,
    toastDismissPause: ToastDismissPause,
    lazyToasterBoxState: LazyToasterBoxState,
    maxVisibleToasts: Int,
) {
    LaunchedEffect(state, lazyToasterBoxState, toastDismissPause) {
        snapshotFlow { state.toasts.map { arrayOf(it.toast, it.state) } }
            .map { lazyToasterBoxState.visibleItemIndices() }
            .collect { visibleIndices ->
                val toasts = state.toasts
                if (toasts.isEmpty() || visibleIndices.isEmpty()) return@collect
                when (toastDismissPause) {
                    ToastDismissPause.Never -> {
                        for (toast in toasts) {
                            state.resumeDismissTimer(toast.toast.id)
                        }
                    }

                    ToastDismissPause.OnNotFront -> {
                        // Resume the dismiss timer for the front toast
                        val frontToastIndex = visibleIndices.last()
                        state.resumeDismissTimer(toasts[frontToastIndex].toast.id)
                        // Pause others
                        for (i in toasts.indices) {
                            if (i != frontToastIndex) {
                                state.pauseDismissTimer(toasts[i].toast.id)
                            }
                        }
                    }

                    ToastDismissPause.OnInvisible -> {
                        val realVisibleIndices = if (visibleIndices.size > maxVisibleToasts) {
                            // Exclude items that are marked as visible but are not,
                            // for the animation reason
                            val from = visibleIndices.size - maxVisibleToasts
                            val to = visibleIndices.size
                            visibleIndices.subList(from, to)
                        } else {
                            visibleIndices
                        }
                        // Resume dismiss timer for visible toasts
                        for (index in realVisibleIndices) {
                            state.resumeDismissTimer(toasts[index].toast.id)
                        }
                        // Pause others
                        val visibleIndexSet = realVisibleIndices.toSet()
                        for (i in toasts.indices) {
                            if (!visibleIndexSet.contains(i)) {
                                state.pauseDismissTimer(toasts[i].toast.id)
                            }
                        }
                    }
                }
            }
    }
}

/**
 * The toast item.
 *
 * @param layoutIndex Starts from the most front item and starts from 0.
 */
@Composable
private fun ToastItem(
    onRequestDismiss: () -> Unit,
    onInvisible: () -> Unit,
    expanded: Boolean,
    layoutIndex: Int,
    toast: Toast,
    dismissing: Boolean,
    maxVisibleToasts: Int,
    widthPolicy: ToastWidthPolicy,
    swipeable: Boolean,
    elevation: Dp,
    shadowAmbientColor: Color,
    shadowSpotColor: Color,
    shape: Shape,
    contentPadding: PaddingValues,
    alignment: Alignment,
    enterTransitionDuration: Int,
    exitTransitionDuration: Int,
    transformHelper: ToastTransformHelper,
    itemHeightProvider: ItemHeightProvider,
    modifier: Modifier = Modifier,
    border: BorderStroke,
    background: Brush,
    iconSlot: @Composable (toast: Toast) -> Unit,
    messageSlot: @Composable (toast: Toast) -> Unit,
    actionSlot: @Composable (toast: Toast) -> Unit,
    closeButton: @Composable (BoxScope.(toast: Toast) -> Unit)?,
) {
    val visibleState = remember {
        MutableTransitionState(false).also { it.targetState = true }
    }

    var frontItemHeight by remember { mutableIntStateOf(itemHeightProvider.get(0)) }

    var height by remember { mutableIntStateOf(0) }

    var dragY by remember { mutableFloatStateOf(0f) }

    val isBottomAlign = alignment.isBottomAlign()

    val draggableState = rememberDraggableState(
        onDelta = { delta -> dragY = max(0f, dragY + delta) }
    )

    fun isSwipedToDismiss(velocity: Float): Boolean {
        if (velocity > 600f && dragY >= height / 5f) return true
        if (velocity > 300f && dragY >= height / 3f) return true
        if (velocity > 100f && dragY >= height / 2f) return true
        return dragY > height * 0.8f
    }

    val scale = animateFloatAsState(
        targetValue = if (!expanded) transformHelper.calcScale(layoutIndex) else 1f,
        animationSpec = tween(durationMillis = exitTransitionDuration),
    )
    val alpha = animateFloatAsState(
        targetValue = if (layoutIndex < maxVisibleToasts) 1f else 0f,
        animationSpec = tween(durationMillis = exitTransitionDuration),
    )
    val tranY = animateFloatAsState(
        targetValue = transformHelper.calcTranslationY(
            itemHeightProvider = itemHeightProvider,
            isBottomAlign = isBottomAlign,
            expanded = expanded,
            layoutIndex = layoutIndex,
        ),
        animationSpec = tween(durationMillis = exitTransitionDuration),
    )

    LaunchedEffect(visibleState, dismissing) {
        if (dismissing) {
            visibleState.targetState = false
        }
    }

    LaunchedEffect(visibleState, onInvisible) {
        // Remove toast after finishing the dismiss transition
        snapshotFlow { visibleState.isIdle && !visibleState.targetState }
            .filter { invisible -> invisible }
            .collect { onInvisible() }
    }

    LaunchedEffect(itemHeightProvider) {
        // Listen to height changes of most front item, so we can adjust our
        // item offset automatically when a new item is added.
        itemHeightProvider.listen(0)
            .collect { frontItemHeight = it }
    }

    AnimatedVisibility(
        visibleState = visibleState,
        enter = slideInVertically(tween(durationMillis = enterTransitionDuration)) {
            if (isBottomAlign) it else -it
        } + fadeIn(tween(durationMillis = enterTransitionDuration)),
        exit = slideOutVertically(tween(durationMillis = exitTransitionDuration)) {
            if (isBottomAlign) it else -it
        } + fadeOut(tween(durationMillis = exitTransitionDuration)),
        modifier = modifier
            .onSizeChanged { height = it.height }
            .graphicsLayer {
                this.alpha = alpha.value
                transformOrigin = TransformOrigin(0.5f, if (isBottomAlign) 0f else 1f)
                scaleX = scale.value
                scaleY = scale.value
                translationY = tranY.value + dragY
            }
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = {},
            )
            .draggable(
                state = draggableState,
                enabled = swipeable,
                orientation = Orientation.Vertical,
                onDragStarted = { dragY = 0f },
                onDragStopped = { velocity ->
                    if (!isSwipedToDismiss(velocity)) {
                        animate(
                            targetValue = 0f,
                            initialValue = dragY,
                            animationSpec = tween(durationMillis = exitTransitionDuration),
                        ) { value, _ ->
                            dragY = value
                        }
                    } else {
                        onRequestDismiss()
                    }
                },
            ),
    ) {
        Box(modifier = Modifier.padding(max(elevation * 1.5f, 10.dp))) {
            Row(
                modifier = Modifier
                    .widthIn(min = widthPolicy.min, max = widthPolicy.max)
                    .let { if (widthPolicy.fillMaxWidth) it.fillMaxWidth() else it }
                    .shadow(
                        elevation = elevation,
                        shape = shape,
                        ambientColor = shadowAmbientColor,
                        spotColor = shadowSpotColor,
                    )
                    .border(
                        width = border.width,
                        brush = border.brush,
                        shape = shape,
                    )
                    .background(
                        brush = background,
                        shape = shape,
                    )
                    .padding(contentPadding),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Row(
                    modifier = Modifier.weight(1f, fill = false),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    iconSlot(toast)
                    messageSlot(toast)
                }
                actionSlot(toast)
            }

            if (closeButton != null) {
                closeButton(toast)
            }
        }
    }
}

/**
 * Provides default values used by the toaster and toasts.
 */
object ToasterDefaults {
    /**
     * Short toast duration, which is 2000ms.
     */
    val DurationShort = 2000.milliseconds

    /**
     * Short toast duration, which is 4000ms.
     */
    val DurationDefault = 4000.milliseconds

    /**
     * Long toast duration, which is 8000ms.
     */
    val DurationLong = 8000.milliseconds

    /**
     * The default toast shape.
     */
    val Shape = RoundedCornerShape(8.dp)

    internal val Elevation = 12.dp
    internal val ShadowAmbientColor = Color.Gray
    internal val DarkShadowAmbientColor = Color.Black
    internal val ShadowSpotColor = Color.Gray
    internal val DarkShadowSpotColor = Color.Black

    internal const val ENTER_TRANSITION_DURATION = 300
    internal const val EXIT_TRANSITION_DURATION = 255

    private val IconSize = 20.dp

    private val ActionButtonShape = RoundedCornerShape(4.dp)

    /**
     * The icon slot that displays an icon for toasts that have a different type than [ToastType.Normal].
     */
    @Composable
    fun iconSlot(toast: Toast) {
        val contentColor = LocalToastContentColor.current
        when (toast.type) {
            ToastType.Normal -> {}
            ToastType.Success -> {
                Box(modifier = Modifier.padding(end = 16.dp)) {
                    Image(
                        imageVector = Icons.Default.CheckCircle,
                        contentDescription = "Success",
                        modifier = Modifier.size(IconSize),
                        colorFilter = ColorFilter.tint(contentColor),
                    )
                }
            }

            ToastType.Info -> {
                Box(modifier = Modifier.padding(end = 16.dp)) {
                    Image(
                        imageVector = Icons.Default.Info,
                        contentDescription = "Info",
                        modifier = Modifier.size(IconSize),
                        colorFilter = ColorFilter.tint(contentColor),
                    )
                }
            }

            ToastType.Warning -> {
                Box(modifier = Modifier.padding(end = 16.dp)) {
                    Image(
                        imageVector = Icons.Default.Warning,
                        contentDescription = "Warning",
                        modifier = Modifier.size(IconSize),
                        colorFilter = ColorFilter.tint(contentColor),
                    )
                }
            }

            ToastType.Error -> {
                Box(modifier = Modifier.padding(end = 16.dp)) {
                    Image(
                        imageVector = Icons.Default.Info,
                        contentDescription = "Error",
                        modifier = Modifier.size(IconSize),
                        colorFilter = ColorFilter.tint(contentColor),
                    )
                }
            }
        }
    }

    /**
     * The message slot that calls [toString] on the toast message and displays the result.
     */
    @Composable
    fun messageSlot(toast: Toast) {
        val contentColor = LocalToastContentColor.current
        BasicText(toast.message.toString(), color = { contentColor })
    }

    /**
     * The action slot that supports [TextToastAction].
     */
    @Composable
    fun actionSlot(toast: Toast) {
        when (val action = toast.action) {
            null -> {}

            is TextToastAction -> {
                ActionButton(
                    onClick = { action.onClick(toast) },
                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                ) {
                    BasicText(action.text, color = { Color.White })
                }
            }

            else -> throw IllegalStateException(
                "Please provide a custom action slot to " +
                        "display this type: ${action::class.simpleName}"
            )
        }
    }

    @Composable
    internal fun contentColor(toast: Toast, richColors: Boolean, darkTheme: Boolean): Color {
        val type = if (richColors) toast.type else ToastType.Normal
        return colorsOf(type, darkTheme).content
    }

    @Composable
    internal fun border(toast: Toast, richColors: Boolean, darkTheme: Boolean): BorderStroke {
        val type = if (richColors) toast.type else ToastType.Normal
        val color = colorsOf(type, darkTheme).border
        return BorderStroke(
            width = 0.8.dp,
            brush = SolidColor(color),
        )
    }

    @Composable
    internal fun background(toast: Toast, richColors: Boolean, darkTheme: Boolean): Brush {
        val type = if (richColors) toast.type else ToastType.Normal
        return SolidColor(colorsOf(type, darkTheme).background)
    }

    @Composable
    private fun ActionButton(
        onClick: () -> Unit,
        modifier: Modifier = Modifier,
        contentPadding: PaddingValues = PaddingValues(0.dp),
        content: @Composable RowScope.() -> Unit,
    ) {
        Row(
            modifier
                .defaultMinSize(
                    minWidth = 64.dp,
                )
                .background(color = Color.Black, shape = ActionButtonShape)
                .clickable { onClick() }
                .padding(contentPadding),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically,
            content = content
        )
    }
}

/**
 * Create a [ToasterState] and remember it.
 *
 * @param onToastDismissed A callback will be called when any toast is dismissed.
 */
@Composable
fun rememberToasterState(
    coroutineScope: CoroutineScope = rememberCoroutineScope(),
    onToastDismissed: ((toast: Toast) -> Unit)? = null,
): ToasterState {
    return remember(coroutineScope) {
        ToasterState(
            coroutineScope = coroutineScope,
            onDismissed = onToastDismissed,
        )
    }.also {
        it.onDismissed = onToastDismissed
    }
}

/**
 * The toaster state, used to show and dismiss toasts.
 *
 * ### Examples:
 *
 * ```kotlin
 * // Show a simple toast
 * val toast = toaster.show("Message")
 *
 * // Show a toast with some parameters
 * toaster.show(
 *     message = "Message",
 *     type = ToastType.Error,
 *     duration = ToasterDefaults.DurationLong,
 * )
 *
 * // Dismiss a toast
 * toaster.dismiss(toast)
 *
 * // Dismiss a toast by id
 * toaster.dismiss(id)
 *
 * // Dismiss all toasts
 * toaster.dismissAll()
 * ```
 */
@Stable
class ToasterState(
    private val coroutineScope: CoroutineScope,
    internal var onDismissed: ((toast: Toast) -> Unit)? = null,
) {
    private val _toasts = mutableStateListOf<StatefulToast>()
    internal val toasts: List<StatefulToast> = _toasts

    private val jobs = mutableMapOf<Any, Job>()

    /**
     * Show a toast from the parameters of [Toast].
     */
    fun show(
        message: Any,
        id: Any = currentNanoTime(),
        icon: Any? = null,
        action: Any? = null,
        type: ToastType = ToastType.Normal,
        duration: Duration = ToasterDefaults.DurationDefault,
    ): Toast {
        val toast = Toast(
            id = id,
            icon = icon,
            message = message,
            action = action,
            type = type,
            duration = duration,
        )
        updateOrShow(toast)
        return toast
    }

    /**
     * Show a toast.
     */
    fun show(toast: Toast) {
        updateOrShow(toast)
    }

    /**
     * Dismiss a toast.
     */
    fun dismiss(toast: Toast) {
        dismiss(toast.id)
    }

    /**
     * Dismiss a toast by id.
     */
    fun dismiss(id: Any) {
        updateToast(id) {
            if (it.isVisible) it.copy(state = VisibleState.Dismissing) else it
        }
        for ((toastId, job) in jobs) {
            if (toastId == id) {
                job.cancel()
                break
            }
        }
    }

    /**
     * Dismiss all toasts.
     */
    fun dismissAll() {
        for (i in 0..toasts.lastIndex) {
            updateToast(toasts[i].toast.id) {
                if (it.isVisible) it.copy(state = VisibleState.Dismissing) else it
            }
        }
        jobs.forEach { (_, job) -> job.cancel() }
    }

    private fun updateOrShow(toast: Toast) {
        val index = toasts.indexOfFirst { it.toast.id == toast.id }
        if (index != -1) {
            val updated = toasts[index].copy(toast = toast, state = VisibleState.Visible)
            _toasts[index] = updated
            startToastJob(toast = toast, displayedTime = updated.displayedTime)
        } else {
            _toasts.add(StatefulToast(toast))
            startToastJob(toast = toast, displayedTime = Duration.ZERO)
        }
    }

    private fun startToastJob(toast: Toast, displayedTime: Duration) {
        val id = toast.id
        jobs[id]?.cancel()
        var delayed = displayedTime
        jobs[id] = coroutineScope.launch {
            var delayDuration = toast.duration
            while (isActive) {
                delay(delayDuration)
                delayed += delayDuration
                // Always get the latest duration
                val expectedDuration = toasts.find { it.toast.id == id }
                    ?.toast?.duration
                    ?: toast.duration
                if (delayed >= expectedDuration) {
                    break
                }
                delayDuration = expectedDuration - delayed
            }
            updateToast(id) { it.copy(state = VisibleState.Dismissing) }
        }.also { job ->
            job.invokeOnCompletion {
                updateToast(id) { it.copy(displayedTime = delayed) }
                // Make sure to remove the job from the map
                if (jobs[id] == job) {
                    jobs.remove(id)
                }
            }
        }
    }

    internal fun markDismissed(id: Any) {
        val index = toasts.indexOfFirst { it.toast.id == id }
        if (index != -1) {
            // Don't remove toast directly, that will break our animations!!!
            updateToast(id) { it.copy(state = VisibleState.Dismissed) }
            onDismissed?.invoke(toasts[index].toast)
            if (jobs[id]?.isActive == true) {
                jobs[id]?.cancel()
            }
            clearToastsIfAllDismissed()
        }
    }

    internal fun pauseDismissTimer(id: Any) {
        val job = jobs[id] ?: return
        job.cancel()
    }

    internal fun resumeDismissTimer(id: Any) {
        if (jobs[id]?.isActive == true) return
        val statefulToast = toasts.firstOrNull { it.toast.id == id } ?: return
        if (statefulToast.displayedTime >= statefulToast.toast.duration) return
        startToastJob(statefulToast.toast, displayedTime = statefulToast.displayedTime)
    }

    internal fun invisibleItemsInRangeFlow(start: Int, end: Int): Flow<Int> {
        fun isOutOfBounds(list: List<*>, start: Int, end: Int): Boolean {
            val indices = list.indices
            return start !in indices || end !in indices
        }

        return snapshotFlow { toasts.map { it.isDismissing || it.isDismissed } }
            .map { list ->
                var count = 0
                if (isOutOfBounds(list, start, end)) {
                    return@map 0
                }
                for (i in start..end) {
                    if (list[i]) {
                        count++
                    }
                }
                count
            }
    }

    private inline fun updateToast(id: Any, block: (current: StatefulToast) -> StatefulToast) {
        val index = toasts.indexOfFirst { it.toast.id == id }
        if (index == -1) return
        val current = toasts[index]
        val updated = block(current)
        if (updated != current) {
            _toasts[index] = updated
        }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    internal fun dismissingToastsFlow(): Flow<StatefulToast> {
        return snapshotFlow { toasts.map { it to it.isDismissing } }
            .flatMapMerge { flowOf(*it.toTypedArray()) }
            .filter { it.second }
            .map { it.first }
    }

    private fun clearToastsIfAllDismissed() {
        if (toasts.all { it.isDismissed }) {
            _toasts.clear()
        }
    }

    /**
     * Listen to the toast state, show/update when the toast is added/updated,
     * and dismiss when it's null.
     *
     * This requires reading the [State] value in the block instead of the normal value.
     */
    @JvmName("listenToState")
    suspend inline fun listen(crossinline readStateBlock: () -> Toast?) {
        listen(snapshotFlow { readStateBlock() })
    }

    /**
     * Listen to the toast collection state, show/update when toasts are added/updated,
     * and dismiss toasts when removed.
     *
     * This requires reading the [State] value in the block instead of the normal value.
     */
    @JvmName("listenToStateMany")
    suspend inline fun listenMany(crossinline readStateBlock: () -> Iterable<Toast>) {
        listenMany(snapshotFlow { readStateBlock() })
    }

    /**
     * Listen to the toast flow, show/update when the toast is added/updated,
     * and dismiss when the flow emits null.
     */
    @JvmName("listenToFlow")
    suspend fun listen(flow: Flow<Toast?>) {
        val mapped = flow.map { if (it != null) listOf(it) else emptyList() }
        listenMany(flow = mapped)
    }

    /**
     * Listen to the toast collection flow, show/update when toasts are added/updated,
     * and dismiss when toasts are removed.
     */
    @JvmName("listenToFlowMany")
    suspend fun listenMany(flow: Flow<Iterable<Toast>>) {
        var previousMap = mutableMapOf<Any, Toast>()
        flow.collect { newList ->
            // Append or update
            val newMap = mutableMapOf<Any, Toast>()
            for (toast in newList) {
                newMap[toast.id] = toast
                val previous = previousMap[toast.id]
                if (previous == null || toast != previous) {
                    show(toast)
                }
            }
            // Remove
            for ((id, _) in previousMap) {
                if (!newMap.contains(id)) {
                    dismiss(id)
                }
            }
            previousMap = newMap
        }
    }
}

internal data class StatefulToast(
    val toast: Toast,
    val state: VisibleState = VisibleState.Visible,
    val displayedTime: Duration = Duration.ZERO,
) {
    val isVisible get() = state == VisibleState.Visible
    val isDismissing get() = state == VisibleState.Dismissing
    val isDismissed get() = state == VisibleState.Dismissed
}

internal enum class VisibleState {
    Visible,
    Dismissing,
    Dismissed,
}

/**
 * Only builtin alignments are supported.
 */
internal fun Alignment.isBottomAlign(): Boolean {
    return this == Alignment.BottomCenter ||
            this == Alignment.BottomStart ||
            this == Alignment.BottomEnd
}