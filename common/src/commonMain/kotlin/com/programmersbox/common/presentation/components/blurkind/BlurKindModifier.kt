package com.programmersbox.common.presentation.components.blurkind

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
import com.kyant.backdrop.backdrops.layerBackdrop
import com.programmersbox.common.DataStore
import dev.chrisbanes.haze.HazeEffectScope
import dev.chrisbanes.haze.hazeEffect
import dev.chrisbanes.haze.hazeSource
import kotlinx.serialization.Serializable
import org.koin.compose.koinInject

/**
 * Creates and remembers the state for managing blur-related UI configurations, such as haze styles,
 * liquid effects, and other blur properties. This function leverages composable and reactive
 * elements to provide a consistent UI state that can be reused across recompositions.
 *
 * @param dataStore The DataStore instance used to retrieve and manage settings or preferences
 *                  required for configuring the blur state. Defaults to a `koinInject()` resolved instance.
 * @param backgroundColor The background color used for rendering the backdrop layer
 *                        in blur-related effects. Defaults to `MaterialTheme.colorScheme.surface`.
 * @return A [BlurKindState] object encapsulating various blur configurations including haze and
 *         liquid blur states, ensuring dynamic and reactive behavior.
 */
@Composable
fun rememberBlurKindState(
    dataStore: DataStore = koinInject(),
    backgroundColor: Color = MaterialTheme.colorScheme.surface,
): BlurKindState {
    val showBlur by dataStore.rememberShowBlur()
    val blurKind by dataStore.rememberBlurKind()
    val blurKindHazeState = rememberBlurKindHazeState(
        dataStore = dataStore,
        showBlur = showBlur,
    )
    val blurKindLiquidState = rememberBlurKindLiquidState(
        dataStore = dataStore,
        backgroundColor = backgroundColor
    )

    return remember(
        blurKind,
        showBlur,
        blurKindLiquidState
    ) {
        BlurKindState(
            blurKind = blurKind,
            showBlur = showBlur,
            hazeState = blurKindHazeState,
            liquidState = blurKindLiquidState
        )
    }
}

/**
 * Represents the state for managing and displaying blur effects of different kinds.
 *
 * This class provides configuration and state management for two main types of blur effects: haze and liquid glass.
 * It allows for toggling the visibility of the blur effect and provides detailed state objects for customizing
 * the behavior and appearance of each blur kind.
 *
 * @constructor Creates a new instance of BlurKindState.
 *
 * @property blurKind Specifies the type of blur effect to apply, either `Haze` or `LiquidGlass`.
 * @property showBlur Determines whether the blur effect should be displayed or not.
 * @property hazeState Holds the state and configuration specific to the `Haze` blur effect, including style and behavior.
 * @property liquidState Contains the settings specific to the `LiquidGlass` blur effect, such as refraction and chromatic properties.
 */
@Stable
class BlurKindState(
    val blurKind: BlurKind,
    val showBlur: Boolean,
    val hazeState: BlurKindHazeState,
    val liquidState: BlurKindLiquidState,
)

/**
 * Represents the types of blur effects that can be applied in a graphical context.
 * This enum is serializable to allow easy persistence and transmission.
 */
@Serializable
enum class BlurKind {
    /**
     * Represents an atmospheric phenomenon where visibility is reduced due to fine particles
     * like dust, smoke, or other pollutants dispersed in the air. This class can be used to
     * model and manage haze-related data and behaviors.
     *
     * Key features include:
     * - Storage of haze-specific attributes such as density, visibility range, and particle composition.
     * - Methods for calculating visibility impact and haze density levels.
     * - Support for interfacing with external systems for haze monitoring or environmental analysis.
     *
     * Intended for applications dealing with environmental simulation, weather forecasting,
     * or air quality monitoring.
     */
    Haze,

    /**
     * Represents the `LiquidGlass` blur kind, typically used for visual effects
     * or UI elements that simulate a frosted glass appearance.
     *
     * This is part of the `BlurKind` enumeration, which categorizes different
     * types of blur effects.
     */
    LiquidGlass
}

/**
 * Configures the modifier to apply a blur effect based on the specified blur kind and its associated properties.
 *
 * @param blurKindState The state object that determines the type of blur effect, its visibility, and specific properties for rendering the effect.
 * @param liquidGlassShape A lambda function specifying the shape for the liquid glass effect. Defaults to a rounded corner shape with a radius of 1
 * dp.
 * @param hazeScope A lambda function defining additional configurations for the haze effect. This block is executed if the blur kind is set to Haze
 * .
 */
fun Modifier.setBlurKind(
    blurKindState: BlurKindState,
    liquidGlassShape: () -> Shape = { RoundedCornerShape(1.dp) },
    hazeScope: HazeEffectScope.() -> Unit = {}
) = if (blurKindState.showBlur) {
    when (blurKindState.blurKind) {
        BlurKind.Haze -> hazeEffect(
            state = blurKindState.hazeState.hazeState,
            style = blurKindState.hazeState.hazeStyle,
            block = hazeScope
        )

        BlurKind.LiquidGlass -> liquidGlassBlur(
            blurKindState = blurKindState,
            liquidGlassShape = liquidGlassShape
        )
    }
} else this

/**
 * Applies a specific type of blur effect to the Modifier based on the provided blurKindState.
 *
 * @param blurKindState The state object that determines the type of blur to apply
 * and whether to show the blur. It contains details about the selected blur kind
 * and its associated configuration.
 */
fun Modifier.setBlurKindSource(blurKindState: BlurKindState) = if (blurKindState.showBlur) {
    when (blurKindState.blurKind) {
        BlurKind.Haze -> hazeSource(blurKindState.hazeState.hazeState)
        BlurKind.LiquidGlass -> layerBackdrop(blurKindState.liquidState.backdrop)
    }
} else this

/**
 * Applies a blur effect to the FloatingActionButton based on the specified blur kind and shape.
 *
 * The method modifies the UI representation of a component by applying one of the blur effects
 * defined in the `BlurKind` enum (`Haze` or `LiquidGlass`). It uses the styling provided by
 * `BlurKindState` and performs additional visual adjustments when the `LiquidGlass` blur kind is selected.
 *
 * @param blurKindState The state object that determines the active blur kind, visibility, and specific
 *                      configuration details for the blur effect.
 * @param shape The shape of the FloatingActionButton which determines the outline boundary where
 *              the blur effect is applied.
 */
fun Modifier.floatingActionButtonBlurKind(
    blurKindState: BlurKindState,
    shape: Shape,
    customBlurAmount: Float = 1f
) = if (blurKindState.showBlur) {
    when (blurKindState.blurKind) {
        BlurKind.Haze -> this
        BlurKind.LiquidGlass -> liquidGlassFABBlur(
            blurKindState = blurKindState,
            customBlurAmount = customBlurAmount,
            shape = shape
        )
    }
} else this