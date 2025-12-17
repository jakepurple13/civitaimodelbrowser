package com.programmersbox.civitaimodelbrowser

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.browser.customtabs.CustomTabsIntent
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.MaterialExpressiveTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MotionScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.expressiveLightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.platform.UriHandler
import androidx.core.net.toUri
import androidx.core.view.WindowCompat
import androidx.fragment.app.FragmentActivity
import com.dokar.sonner.Toaster
import com.dokar.sonner.ToasterState
import com.materialkolor.PaletteStyle
import com.materialkolor.dynamicColorScheme
import com.materialkolor.dynamiccolor.ColorSpec
import com.materialkolor.ktx.animateColorScheme
import com.programmersbox.common.DataStore
import com.programmersbox.common.ThemeMode
import com.programmersbox.common.UIShow
import io.kamel.core.config.KamelConfig
import io.kamel.core.config.takeFrom
import io.kamel.image.config.Default
import io.kamel.image.config.LocalKamelConfig
import io.kamel.image.config.animatedImageDecoder
import io.kamel.image.config.imageBitmapResizingDecoder
import io.kamel.image.config.resourcesFetcher
import org.koin.compose.koinInject

class MainActivity : FragmentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        WindowCompat.setDecorFitsSystemWindows(window, false)
        enableEdgeToEdge()
        setContent {
            val isDarkMode by koinInject<DataStore>().rememberThemeMode()
            CustomMaterialTheme(
                darkTheme = isDarkMode
            ) {
                val toaster = koinInject<ToasterState>()
                val defaultUriHandler = LocalUriHandler.current
                val customUriHandler =
                    remember { customTabsUriHandler { defaultUriHandler.openUri(it) } }
                CompositionLocalProvider(
                    LocalUriHandler provides customUriHandler,
                    LocalKamelConfig provides customKamelConfig()
                ) {
                    UIShow(
                        onShareClick = {
                            startActivity(
                                Intent.createChooser(
                                    Intent().apply {
                                        action = Intent.ACTION_SEND
                                        putExtra(Intent.EXTRA_TEXT, it)
                                        type = "text/plain"
                                    },
                                    null
                                )
                            )
                        }
                    )
                }
                Toaster(
                    state = toaster,
                    richColors = true
                )
            }
        }
    }
}

fun Context.customTabsUriHandler(
    onFailure: (String) -> Unit,
) = object : UriHandler {
    override fun openUri(uri: String) {
        runCatching {
            CustomTabsIntent.Builder()
                .setExitAnimations(
                    this@customTabsUriHandler,
                    android.R.anim.slide_in_left,
                    android.R.anim.slide_out_right
                )
                .setShareState(CustomTabsIntent.SHARE_STATE_ON)
                .build()
                .launchUrl(this@customTabsUriHandler, uri.toUri())
        }.onFailure { onFailure(uri) }
    }
}

@Composable
private fun customKamelConfig(): KamelConfig {
    val context = LocalContext.current
    return KamelConfig {
        takeFrom(KamelConfig.Default)
        imageBitmapResizingDecoder()
        animatedImageDecoder()
        resourcesFetcher(context)
    }
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun CustomMaterialTheme(
    darkTheme: ThemeMode,
    dynamicColor: Boolean = true,
    content: @Composable () -> Unit,
) {
    val systemDarkTheme = isSystemInDarkTheme()
    val colorScheme = if (dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        val context = LocalContext.current
        when (darkTheme) {
            ThemeMode.System -> if (systemDarkTheme)
                dynamicDarkColorScheme(context)
            else
                dynamicLightColorScheme(context)

            ThemeMode.Light -> dynamicLightColorScheme(context)
            ThemeMode.Dark -> dynamicDarkColorScheme(context)
        }
    } else {
        when (darkTheme) {
            ThemeMode.System -> if (systemDarkTheme)
                dynamicColorScheme(
                    primary = Color.Cyan,
                    isDark = true,
                    style = PaletteStyle.Expressive,
                    specVersion = ColorSpec.SpecVersion.SPEC_2025
                )
            else
                expressiveLightColorScheme()

            ThemeMode.Dark -> dynamicColorScheme(
                primary = Color.Cyan,
                isDark = true,
                style = PaletteStyle.Expressive,
                specVersion = ColorSpec.SpecVersion.SPEC_2025
            )
            ThemeMode.Light -> expressiveLightColorScheme()
        }
    }

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val activity = view.context as Activity
            activity.window.statusBarColor = Color.Transparent.toArgb()
            WindowCompat.getInsetsController(activity.window, view).isAppearanceLightStatusBars =
                !when (darkTheme) {
                    ThemeMode.System -> systemDarkTheme
                    ThemeMode.Light -> false
                    ThemeMode.Dark -> true
                }
        }
    }

    MaterialExpressiveTheme(
        colorScheme = animateColorScheme(colorScheme),
        typography = MaterialTheme.typography,
        motionScheme = MotionScheme.expressive(),
        content = content
    )
}
