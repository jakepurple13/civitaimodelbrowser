package com.programmersbox.civitaimodelbrowser

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.browser.customtabs.CustomTabsIntent
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.platform.UriHandler
import androidx.core.net.toUri
import androidx.core.view.WindowCompat
import com.dokar.sonner.Toaster
import com.dokar.sonner.ToasterState
import com.programmersbox.common.UIShow
import io.kamel.core.config.KamelConfig
import io.kamel.core.config.takeFrom
import io.kamel.image.config.Default
import io.kamel.image.config.LocalKamelConfig
import io.kamel.image.config.animatedImageDecoder
import io.kamel.image.config.imageBitmapResizingDecoder
import io.kamel.image.config.resourcesFetcher
import org.koin.compose.koinInject

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        WindowCompat.setDecorFitsSystemWindows(window, false)
        enableEdgeToEdge()
        setContent {
            CustomMaterialTheme {
                val toaster = koinInject<ToasterState>()
                val defaultUriHandler = LocalUriHandler.current
                val customUriHandler = remember { customTabsUriHandler { defaultUriHandler.openUri(it) } }
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

@Composable
fun CustomMaterialTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = true,
    content: @Composable () -> Unit,
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }

        darkTheme -> darkColorScheme()
        else -> lightColorScheme()
    }
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val activity = view.context as Activity
            activity.window.statusBarColor = Color.Transparent.toArgb()
            WindowCompat.getInsetsController(activity.window, view).isAppearanceLightStatusBars = !darkTheme
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = MaterialTheme.typography,
        content = content
    )
}
