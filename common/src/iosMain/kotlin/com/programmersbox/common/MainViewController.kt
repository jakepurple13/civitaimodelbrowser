package com.programmersbox.common

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.MaterialExpressiveTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MotionScheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.Typography
import androidx.compose.material3.expressiveLightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.window.ComposeUIViewController
import com.materialkolor.PaletteStyle
import com.materialkolor.dynamicColorScheme
import com.materialkolor.dynamiccolor.ColorSpec
import com.materialkolor.ktx.animateColorScheme
import com.programmersbox.common.di.cmpModules
import com.programmersbox.common.presentation.backup.Zipper
import com.programmersbox.common.presentation.components.Toaster
import com.programmersbox.common.presentation.components.ToasterState
import com.programmersbox.common.presentation.qrcode.QrCodeRepository
import kotlinx.cinterop.ExperimentalForeignApi
import org.koin.compose.KoinApplication
import org.koin.compose.koinInject
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.koinConfiguration
import org.koin.dsl.module
import platform.Foundation.NSDocumentDirectory
import platform.Foundation.NSFileManager
import platform.Foundation.NSURL
import platform.Foundation.NSUserDomainMask

@OptIn(ExperimentalForeignApi::class)
fun MainViewController() = ComposeUIViewController {
    KoinApplication(
        configuration = koinConfiguration(
            declaration = {
                modules(
                    cmpModules(),
                    createPlatformModule(),
                    module {
                        factory { ApplicationInfo("4.3.5") }
                        single<() -> String> {
                            {
                                val documentDirectory: NSURL? = NSFileManager
                                    .defaultManager
                                    .URLForDirectory(
                                        directory = NSDocumentDirectory,
                                        inDomain = NSUserDomainMask,
                                        appropriateForURL = null,
                                        create = false,
                                        error = null,
                                    )
                                requireNotNull(documentDirectory).path + "/androidx.preferences_pb"
                            }
                        }
                        single { getDatabaseBuilder() }
                        singleOf(::QrCodeRepository)
                        singleOf(::Zipper)
                    }
                )
            }
        )
    ) {
        val isDarkMode by koinInject<DataStore>().rememberThemeMode()
        CustomMaterialTheme(
            darkTheme = isDarkMode
        ) {
            App(
                onShareClick = {}
            )

            Toaster(
                state = koinInject<ToasterState>(),
                richColors = true
            )
        }
    }
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun CustomMaterialTheme(
    darkTheme: ThemeMode,
    shapes: Shapes = MaterialTheme.shapes,
    typography: Typography = MaterialTheme.typography,
    content: @Composable () -> Unit,
) {
    val isDarkMode = isSystemInDarkTheme()
    MaterialExpressiveTheme(
        colorScheme = animateColorScheme(
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
            }
        ),
        shapes = shapes,
        typography = typography,
        motionScheme = MotionScheme.expressive(),
        content = content
    )
}
