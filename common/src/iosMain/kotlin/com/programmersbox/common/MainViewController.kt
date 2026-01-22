package com.programmersbox.common

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.MaterialExpressiveTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MotionScheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.Typography
import androidx.compose.material3.expressiveLightColorScheme
import androidx.compose.material3.windowsizeclass.ExperimentalMaterial3WindowSizeClassApi
import androidx.compose.material3.windowsizeclass.calculateWindowSizeClass
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.window.ComposeUIViewController
import com.materialkolor.PaletteStyle
import com.materialkolor.dynamicColorScheme
import com.materialkolor.dynamiccolor.ColorSpec
import com.materialkolor.ktx.animateColorScheme
import com.programmersbox.common.di.cmpModules
import com.programmersbox.common.presentation.backup.BackupRepository
import com.programmersbox.common.presentation.backup.RestoreInfo
import com.programmersbox.common.presentation.backup.Zipper
import com.programmersbox.common.presentation.components.ToastType
import com.programmersbox.common.presentation.components.Toaster
import com.programmersbox.common.presentation.components.ToasterState
import com.programmersbox.common.presentation.qrcode.QrCodeRepository
import dev.brewkits.kmpworkmanager.background.data.IosWorker
import dev.brewkits.kmpworkmanager.background.data.IosWorkerFactory
import dev.brewkits.kmpworkmanager.kmpWorkerModule
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.serialization.json.Json
import nl.adaptivity.xmlutil.core.impl.multiplatform.name
import org.koin.compose.KoinApplication
import org.koin.compose.koinInject
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.koinConfiguration
import org.koin.dsl.module
import platform.Foundation.NSDocumentDirectory
import platform.Foundation.NSFileManager
import platform.Foundation.NSURL
import platform.Foundation.NSUserDomainMask
import kotlin.time.measureTime

@OptIn(ExperimentalForeignApi::class, ExperimentalMaterial3WindowSizeClassApi::class)
fun MainViewController() = ComposeUIViewController {
    KoinApplication(
        configuration = koinConfiguration(
            declaration = {
                modules(
                    cmpModules(),
                    createPlatformModule(),
                    kmpWorkerModule(MyWorkerFactory(), setOf("restoring")),
                    module {
                        factory { ApplicationInfo(BuildKonfig.VERSION_NAME) }
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
        val dataStore = koinInject<DataStore>()
        val isDarkMode by dataStore.rememberThemeMode()
        val isAmoled by dataStore.rememberIsAmoled()
        CustomMaterialTheme(
            darkTheme = isDarkMode,
            isAmoled = isAmoled
        ) {
            CompositionLocalProvider(
                LocalWindowClassSize provides calculateWindowSizeClass().widthSizeClass
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
}

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
        colorScheme = animateColorScheme(
            remember(darkTheme, isDarkMode, isAmoled) {
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
                }.let { colorScheme ->
                    isAmoledMode(
                        colorScheme = colorScheme,
                        isDarkMode = (isDarkMode && darkTheme == ThemeMode.System) || darkTheme == ThemeMode.Dark,
                        isAmoled = isAmoled
                    )
                }
            }
        ),
        shapes = shapes,
        typography = typography,
        motionScheme = MotionScheme.expressive(),
        content = content
    )
}

class RestoreWorker : IosWorker, KoinComponent {
    private val backupRepository: BackupRepository by inject()
    private val toasterState: ToasterState by inject()
    private val notificationHandler: NotificationHandler by inject()
    override suspend fun doWork(input: String?): Boolean {
        println("Restore Worker: $input")
        val inputData = Json.decodeFromString<RestoreInfo>(input ?: return false)
        val platformFile = inputData.platformFile
        val includeFavorites = inputData.includeFavorites
        val includeBlacklisted = inputData.includeBlacklisted
        val includeSettings = inputData.includeSettings
        val includeSearchHistory = inputData.includeSearchHistory
        val listsToInclude = inputData.listItemsByUuid

        println("Will restore $includeFavorites $includeBlacklisted $includeSettings $includeSearchHistory")
        println("Will restore lists: ${listsToInclude.size}")

        println("Restoring $platformFile")
        val readItems = backupRepository.readItems(platformFile)
        val duration = measureTime {
            backupRepository.restoreItems(
                backupItems = readItems.copy(lists = readItems.lists?.filter { it.item.uuid in listsToInclude }),
                includeSettings = includeSettings,
                includeFavorites = includeFavorites,
                includeBlacklisted = includeBlacklisted,
                includeSearchHistory = includeSearchHistory,
            )
        }
        println("Restored in $duration")

        notificationHandler.notify(
            title = "Restore Complete",
            message = "Restore Complete in $duration",
            uuid = "restore_complete",
        )

        toasterState.show(
            "Backup Complete in $duration",
            type = ToastType.Success
        )

        return true
    }
}

class MyWorkerFactory : IosWorkerFactory {
    override fun createWorker(workerClassName: String): IosWorker? {
        println("Worker Factory: $workerClassName")
        return when (workerClassName) {
            RestoreWorker::class.name -> RestoreWorker()
            else -> null
        }
    }
}