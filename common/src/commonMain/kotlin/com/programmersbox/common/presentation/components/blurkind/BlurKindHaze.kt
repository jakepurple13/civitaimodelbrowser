package com.programmersbox.common.presentation.components.blurkind

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import com.programmersbox.common.DataStore
import dev.chrisbanes.haze.HazeState
import dev.chrisbanes.haze.HazeStyle
import dev.chrisbanes.haze.rememberHazeState
import org.koin.compose.koinInject


@Composable
fun rememberBlurKindHazeState(
    showBlur: Boolean,
    dataStore: DataStore = koinInject(),
): BlurKindHazeState {
    val useProgressive by dataStore.rememberUseProgressive()

    val blurType by dataStore.rememberBlurType()

    val hazeState = rememberHazeState(showBlur)
    val hazeStyle = blurType.toHazeStyle()

    return remember(
        hazeState,
        hazeStyle,
        showBlur,
    ) {
        BlurKindHazeState(
            hazeState = hazeState,
            hazeStyle = hazeStyle,
            useProgressive = useProgressive
        )
    }
}

/**
 * Represents the state configuration for applying a blur effect with haze.
 *
 * @property hazeState Describes the haze parameters such as intensity or behavior within the blur effect.
 * @property hazeStyle Defines the visual style of the haze effect, controlling its appearance.
 * @property useProgressive Indicates whether the blur effect should be applied progressively.
 */
@Stable
class BlurKindHazeState(
    val hazeState: HazeState,
    val hazeStyle: HazeStyle,
    val useProgressive: Boolean,
)
