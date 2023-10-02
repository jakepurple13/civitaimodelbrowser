import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

val DarkColorScheme = darkColorScheme(
    primary = Color(0xff8bd0f0),
    onPrimary = Color(0xff003546),
    primaryContainer = Color(0xff004d64),
    onPrimaryContainer = Color(0xffbee9ff),
    inversePrimary = Color(0xff126682),
    secondary = Color(0xffb4cad6),
    onSecondary = Color(0xff1f333c),
    secondaryContainer = Color(0xff354a54),
    onSecondaryContainer = Color(0xffd0e6f2),
    tertiary = Color(0xffc6c2ea),
    onTertiary = Color(0xff2f2d4d),
    tertiaryContainer = Color(0xff454364),
    onTertiaryContainer = Color(0xffe3dfff),
    background = Color(0xff191c1e),
    onBackground = Color(0xffe1e2e4),
    surface = Color(0xff191c1e),
    onSurface = Color(0xffe1e2e4),
    surfaceVariant = Color(0xff40484c),
    onSurfaceVariant = Color(0xffc5c7c9),
    inverseSurface = Color(0xffe1e2e4),
    inverseOnSurface = Color(0xff2e3133),
    outline = Color(0xff8a9297),
)

val LightColorScheme = lightColorScheme(
    primary = Color(0xff126682),
    onPrimary = Color(0xffffffff),
    primaryContainer = Color(0xffbee9ff),
    onPrimaryContainer = Color(0xff001f2a),
    inversePrimary = Color(0xff8bd0f0),
    secondary = Color(0xff4d616c),
    onSecondary = Color(0xffffffff),
    secondaryContainer = Color(0xffd0e6f2),
    onSecondaryContainer = Color(0xff081e27),
    tertiary = Color(0xff5d5b7d),
    onTertiary = Color(0xffffffff),
    tertiaryContainer = Color(0xffe3dfff),
    onTertiaryContainer = Color(0xff1a1836),
    background = Color(0xfffbfcfe),
    onBackground = Color(0xff191c1e),
    surface = Color(0xfffbfcfe),
    onSurface = Color(0xff191c1e),
    surfaceVariant = Color(0xffdce4e9),
    onSurfaceVariant = Color(0xff40484c),
    inverseSurface = Color(0xff2e3133),
    inverseOnSurface = Color(0xffeff1f3),
    outline = Color(0xff6f777c),
)

@Composable
fun CustomMaterialTheme(
    shapes: Shapes = MaterialTheme.shapes,
    typography: Typography = MaterialTheme.typography,
    content: @Composable () -> Unit,
) = MaterialTheme(
    colorScheme = if (isSystemInDarkTheme()) DarkColorScheme else LightColorScheme,
    shapes = shapes,
    typography = typography,
    content = content
)