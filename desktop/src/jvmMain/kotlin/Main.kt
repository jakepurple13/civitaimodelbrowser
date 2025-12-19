import androidx.compose.runtime.Composable
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.ui.window.MenuBar
import androidx.compose.ui.window.Tray
import androidx.compose.ui.window.TrayState
import androidx.compose.ui.window.application
import androidx.navigation3.runtime.NavKey
import ca.gosyer.appdirs.AppDirs
import com.dokar.sonner.Toaster
import com.dokar.sonner.ToasterState
import com.programmersbox.common.ApplicationInfo
import com.programmersbox.common.Screen
import com.programmersbox.common.UIShow
import com.programmersbox.common.backup.BackupScreen
import com.programmersbox.common.backup.RestoreScreen
import com.programmersbox.common.backup.Zipper
import com.programmersbox.common.createPlatformModule
import com.programmersbox.common.di.NavigationHandler
import com.programmersbox.common.di.cmpModules
import com.programmersbox.common.getDatabaseBuilder
import com.programmersbox.common.qrcode.QrCodeRepository
import com.programmersbox.common.settings.AboutScreen
import com.programmersbox.common.settings.BehaviorSettingsScreen
import com.programmersbox.common.settings.NsfwSettingsScreen
import com.programmersbox.common.settings.StatsScreen
import com.programmersbox.desktop.BuildKonfig
import com.programmersbox.resources.Res
import com.programmersbox.resources.civitai_logo
import org.jetbrains.compose.resources.painterResource
import org.koin.compose.KoinApplication
import org.koin.compose.koinInject
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.koinConfiguration
import org.koin.dsl.module
import java.awt.Toolkit
import java.awt.datatransfer.StringSelection
import java.io.File

fun main() {
    System.setProperty("apple.awt.application.appearance", "system")
    application {
        KoinApplication(
            configuration = koinConfiguration(
                declaration = {
                    modules(
                        cmpModules(),
                        createPlatformModule(),
                        module {
                            //single { producePath() }
                            factory { ApplicationInfo(BuildKonfig.VERSION_NAME) }
                            single {
                                AppDirs {
                                    appName = "CivitAiModelBrowser"
                                    appAuthor = "jakepurple13"
                                }
                            }
                            single {
                                {
                                    File(
                                        get<AppDirs>().getUserDataDir(),
                                        "androidx.preferences_pb"
                                    ).absolutePath
                                }
                            }
                            single { getDatabaseBuilder(get()) }
                            single { TrayState() }
                            singleOf(::QrCodeRepository)
                            singleOf(::Zipper)
                        }
                    )
                }
            ),
            content = {
                val navHandler = koinInject<NavigationHandler>()
                val toaster = koinInject<ToasterState>()
                Tray(
                    state = koinInject<TrayState>(),
                    icon = painterResource(Res.drawable.civitai_logo),
                    menu = {
                        Item(
                            "Home",
                            onClick = {
                                navHandler.backStack.clear()
                                navHandler.backStack.add(Screen.List)
                            },
                            enabled = navHandler.backStack.lastOrNull() != Screen.List,
                        )
                        Item(
                            "Favorites",
                            onClick = { navHandler.backStack.add(Screen.Favorites) },
                            enabled = navHandler.backStack.lastOrNull() != Screen.Favorites,
                        )
                        Item(
                            "Stats",
                            onClick = { navHandler.backStack.add(Screen.Settings.Stats) },
                            enabled = navHandler.backStack.lastOrNull() != Screen.Settings.Stats,
                        )
                        Item(
                            "Blacklisted",
                            onClick = { navHandler.backStack.add(Screen.Settings.Blacklisted) },
                            enabled = navHandler.backStack.lastOrNull() != Screen.Settings.Blacklisted,
                        )
                        Item(
                            "Settings",
                            onClick = { navHandler.backStack.add(Screen.Settings) },
                            enabled = navHandler.backStack.lastOrNull() != Screen.Settings,
                        )
                        Separator()
                        Item(
                            "Exit",
                            onClick = ::exitApplication
                        )
                    }
                )

                val appState = remember { MyApplicationState() }

                for (window in appState.windows) {
                    key(window) {
                        MyWindow(window)
                    }
                }

                WindowWithBar(
                    onCloseRequest = ::exitApplication,
                    frameWindowScope = {
                        MenuBar {
                            Menu("File") {
                                Item(
                                    "New Stats Window",
                                    onClick = { appState.openNewWindow(Screen.Settings.Stats) }
                                )
                                Item(
                                    "New Backup Window",
                                    onClick = { appState.openNewWindow(Screen.Settings.Backup) }
                                )
                                Item(
                                    "New Restore Window",
                                    onClick = { appState.openNewWindow(Screen.Settings.Restore) }
                                )
                                Item(
                                    "New About Window",
                                    onClick = { appState.openNewWindow(Screen.Settings.About) }
                                )
                                Menu("Settings") {
                                    Item(
                                        "New Nsfw Settings Window",
                                        onClick = { appState.openNewWindow(Screen.Settings.Nsfw) }
                                    )
                                    Item(
                                        "New Behavior Settings Window",
                                        onClick = { appState.openNewWindow(Screen.Settings.Behavior) }
                                    )
                                }
                                Separator()
                                Item(
                                    "Exit",
                                    onClick = ::exitApplication
                                )
                            }
                            Menu("View") {
                                Item(
                                    "Home",
                                    onClick = {
                                        navHandler.backStack.clear()
                                        navHandler.backStack.add(Screen.List)
                                    },
                                    enabled = navHandler.backStack.lastOrNull() != Screen.List,
                                )
                                Item(
                                    "Favorites",
                                    onClick = { navHandler.backStack.add(Screen.Favorites) },
                                    enabled = navHandler.backStack.lastOrNull() != Screen.Favorites,
                                )
                                Item(
                                    "Search",
                                    onClick = { navHandler.backStack.add(Screen.Search) },
                                    enabled = navHandler.backStack.lastOrNull() != Screen.Search,
                                )
                                Item(
                                    "Stats",
                                    onClick = { navHandler.backStack.add(Screen.Settings.Stats) },
                                    enabled = navHandler.backStack.lastOrNull() != Screen.Settings.Stats,
                                )
                                Item(
                                    "Blacklisted",
                                    onClick = { navHandler.backStack.add(Screen.Settings.Blacklisted) },
                                    enabled = navHandler.backStack.lastOrNull() != Screen.Settings.Blacklisted,
                                )
                                Item(
                                    "Settings",
                                    onClick = { navHandler.backStack.add(Screen.Settings) },
                                    enabled = navHandler.backStack.lastOrNull() != Screen.Settings,
                                )
                            }
                        }
                    }
                ) {
                    UIShow(
                        onShareClick = { link ->
                            Toolkit.getDefaultToolkit().also {
                                it
                                    .systemClipboard
                                    .setContents(StringSelection(link), null)
                                it.beep()
                            }
                        },
                        producePath = { "androidx.preferences_pb" },
                    )

                    Toaster(
                        state = toaster,
                        richColors = true
                    )
                }
            }
        )
    }
}

@Composable
private fun MyWindow(
    state: MyWindowState,
) = WindowWithBar(
    onCloseRequest = state::close
) {
    when (state.destination) {
        Screen.Settings.Stats -> StatsScreen()
        Screen.Settings.Backup -> BackupScreen()
        Screen.Settings.Restore -> RestoreScreen()
        Screen.Settings.About -> AboutScreen()
        Screen.Settings.Nsfw -> NsfwSettingsScreen()
        Screen.Settings.Behavior -> BehaviorSettingsScreen()
    }
}

private class MyApplicationState {
    val windows = mutableStateListOf<MyWindowState>()

    fun openNewWindow(
        destination: NavKey
    ) {
        windows += MyWindowState(destination)
    }

    private fun MyWindowState(
        title: NavKey
    ) = MyWindowState(
        title,
        windows::remove
    )
}

private class MyWindowState(
    val destination: NavKey,
    private val close: (MyWindowState) -> Unit
) {
    fun close() = close(this)
}