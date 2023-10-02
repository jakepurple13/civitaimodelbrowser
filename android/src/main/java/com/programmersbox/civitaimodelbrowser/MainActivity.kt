package com.programmersbox.civitaimodelbrowser

import android.app.Activity
import android.content.Intent
import android.os.Build
import android.os.Bundle
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat
import com.programmersbox.common.UIShow
import moe.tlaster.precompose.lifecycle.PreComposeActivity
import moe.tlaster.precompose.lifecycle.setContent

class MainActivity : PreComposeActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        WindowCompat.setDecorFitsSystemWindows(window, false)
        setContent {
            CustomMaterialTheme {
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
        }
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
            activity.window.statusBarColor = colorScheme.primary.toArgb()
            WindowCompat.getInsetsController(activity.window, view).isAppearanceLightStatusBars = darkTheme
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = MaterialTheme.typography,
        content = content
    )
}
