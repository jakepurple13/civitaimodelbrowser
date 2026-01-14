import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.MaterialExpressiveTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MotionScheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.Typography
import androidx.compose.material3.expressiveLightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.Color
import com.materialkolor.PaletteStyle
import com.materialkolor.dynamicColorScheme
import com.materialkolor.dynamiccolor.ColorSpec
import com.programmersbox.common.ThemeMode
import com.programmersbox.common.isAmoledMode

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun CustomMaterialTheme(
    darkTheme: ThemeMode,
    isAmoled: Boolean,
    shapes: Shapes = MaterialTheme.shapes,
    typography: Typography = MaterialTheme.typography,
    content: @Composable () -> Unit,
) {
    val isDarkMode = isSystemInDarkTheme()
    MaterialExpressiveTheme(
        colorScheme = isAmoledMode(
            remember(darkTheme, isDarkMode) {
                when (darkTheme) {
                    ThemeMode.System -> if (isDarkMode)
                        dynamicColorScheme(
                            seedColor = Color.Cyan,
                            isDark = true,
                            style = PaletteStyle.Expressive,
                            specVersion = ColorSpec.SpecVersion.SPEC_2025
                        )
                    else
                        expressiveLightColorScheme()

                    ThemeMode.Dark -> dynamicColorScheme(
                        seedColor = Color.Cyan,
                        isDark = true,
                        style = PaletteStyle.Expressive,
                        specVersion = ColorSpec.SpecVersion.SPEC_2025
                    )

                    ThemeMode.Light -> expressiveLightColorScheme()
                }
            },
            darkTheme == ThemeMode.Dark || isDarkMode,
            isAmoled
        ),
        shapes = shapes,
        typography = typography,
        motionScheme = MotionScheme.expressive(),
        content = content
    )
}