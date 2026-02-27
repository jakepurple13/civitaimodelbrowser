package com.programmersbox.civitaimodelbrowser

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.platform.UriHandler
import androidx.core.net.toUri
import androidx.core.view.WindowCompat
import androidx.fragment.app.FragmentActivity
import com.materialkolor.PaletteStyle
import com.materialkolor.dynamicColorScheme
import com.materialkolor.dynamiccolor.ColorSpec
import com.materialkolor.ktx.animateColorScheme
import com.programmersbox.common.DataStore
import com.programmersbox.common.Screen
import com.programmersbox.common.ThemeMode
import com.programmersbox.common.UIShow
import com.programmersbox.common.di.NavigationHandler
import com.programmersbox.common.isAmoledMode
import com.programmersbox.common.presentation.components.Toaster
import com.programmersbox.common.presentation.components.ToasterState
import io.kamel.core.config.KamelConfig
import io.kamel.core.config.takeFrom
import io.kamel.image.config.Default
import io.kamel.image.config.LocalKamelConfig
import io.kamel.image.config.animatedImageDecoder
import io.kamel.image.config.imageBitmapResizingDecoder
import io.kamel.image.config.resourcesFetcher
import org.koin.compose.koinInject

class MainActivity : FragmentActivity() {
    private var deepLinkUri by mutableStateOf<Uri?>(null)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        deepLinkUri = intent?.data
        WindowCompat.setDecorFitsSystemWindows(window, false)
        enableEdgeToEdge()
        setContent {
            val notificationPermission = rememberLauncherForActivityResult(
                ActivityResultContracts.RequestPermission()
            ) { }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                SideEffect {
                    notificationPermission.launch(android.Manifest.permission.POST_NOTIFICATIONS)
                }
            }
            val dataStore = koinInject<DataStore>()

            val isDarkMode by dataStore.rememberThemeMode()
            val isAmoled by dataStore.rememberIsAmoled()
            CustomMaterialTheme(
                darkTheme = isDarkMode,
                isAmoled = isAmoled
            ) {
                val toaster = koinInject<ToasterState>()
                val defaultUriHandler = LocalUriHandler.current
                val navHandler = koinInject<NavigationHandler>()

                // Handle deep link navigation
                LaunchedEffect(deepLinkUri) {
                    val uri = deepLinkUri ?: return@LaunchedEffect
                    deepLinkUri = null
                    handleDeepLink(uri, navHandler)
                }

                val customUriHandler = remember {
                    customTabsUriHandler(
                        onFailure = { defaultUriHandler.openUri(it) },
                        onFallback = { navHandler.backStack.add(Screen.WebView(it)) }
                    )
                }
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
                        },
                        activity = this
                    )
                }
                Toaster(
                    state = toaster,
                    richColors = true
                )
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        deepLinkUri = intent.data
    }
}

private fun handleDeepLink(uri: Uri, navHandler: NavigationHandler) {
    if (uri.scheme != "civitai-browser") return
    when (uri.host) {
        "model" -> {
            val modelId = uri.pathSegments.firstOrNull() ?: return
            navHandler.backStack.add(Screen.Detail(modelId))
        }
        "user" -> {
            val username = uri.pathSegments.firstOrNull() ?: return
            navHandler.backStack.add(Screen.User(username))
        }
        "image" -> {
            val modelId = uri.pathSegments.firstOrNull() ?: return
            val modelName = uri.getQueryParameter("name").orEmpty()
            navHandler.backStack.add(Screen.DetailsImage(modelId, modelName))
        }
    }
}

fun Context.customTabsUriHandler(
    onFailure: (String) -> Unit,
    onFallback: (String) -> Unit = onFailure
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
        }
            .recoverCatching { onFailure(uri) }
            .onFailure { onFallback(uri) }
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
    isAmoled: Boolean,
    dynamicColor: Boolean = true,
    content: @Composable () -> Unit,
) {
    val systemDarkTheme = isSystemInDarkTheme()
    val context = LocalContext.current
    val colorScheme = remember(darkTheme, systemDarkTheme, dynamicColor, isAmoled) {
        if (dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
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
        }.let { colorScheme ->
            isAmoledMode(
                colorScheme = colorScheme,
                isDarkMode = (systemDarkTheme && darkTheme == ThemeMode.System) || darkTheme == ThemeMode.Dark,
                isAmoled = isAmoled
            )
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
