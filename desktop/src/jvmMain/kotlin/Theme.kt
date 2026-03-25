import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.MaterialExpressiveTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MotionScheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.Typography
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import com.materialkolor.ktx.animateColorScheme
import com.programmersbox.common.ThemeColor
import com.programmersbox.common.ThemeMode
import com.programmersbox.common.buildColorScheme

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun CustomMaterialTheme(
    darkTheme: ThemeMode,
    isAmoled: Boolean,
    themeColor: ThemeColor,
    shapes: Shapes = MaterialTheme.shapes,
    typography: Typography = MaterialTheme.typography,
    content: @Composable () -> Unit,
) {
    val isDarkMode = isSystemInDarkTheme()
    MaterialExpressiveTheme(
        colorScheme = animateColorScheme(
            remember(darkTheme, isDarkMode, isAmoled, themeColor) {
                buildColorScheme(
                    colorScheme = null,
                    darkTheme = darkTheme,
                    isAmoled = isAmoled,
                    themeColor = themeColor,
                    systemDarkTheme = isDarkMode
                )
            }
        ),
        shapes = shapes,
        typography = typography,
        motionScheme = MotionScheme.expressive(),
        content = content
    )
}