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
import com.programmersbox.common.ApplicationInfo
import com.programmersbox.common.Screen
import com.programmersbox.common.UIShow
import com.programmersbox.common.createPlatformModule
import com.programmersbox.common.di.NavigationHandler
import com.programmersbox.common.di.cmpModules
import com.programmersbox.common.getDatabaseBuilder
import com.programmersbox.common.presentation.backup.BackupScreen
import com.programmersbox.common.presentation.backup.RestoreScreen
import com.programmersbox.common.presentation.backup.Zipper
import com.programmersbox.common.presentation.components.Toaster
import com.programmersbox.common.presentation.components.ToasterState
import com.programmersbox.common.presentation.qrcode.QrCodeRepository
import com.programmersbox.common.presentation.settings.AboutScreen
import com.programmersbox.common.presentation.settings.BehaviorSettingsScreen
import com.programmersbox.common.presentation.settings.NsfwSettingsScreen
import com.programmersbox.common.presentation.settings.StatsScreen
import com.programmersbox.desktop.BuildKonfig
import com.programmersbox.desktop.resources.DesktopResources
import com.programmersbox.desktop.resources.new_about_window
import com.programmersbox.desktop.resources.new_backup_window
import com.programmersbox.desktop.resources.new_behavior_settings_window
import com.programmersbox.desktop.resources.new_nsfw_settings_window
import com.programmersbox.desktop.resources.new_restore_window
import com.programmersbox.desktop.resources.new_stats_window
import com.programmersbox.resources.Res
import com.programmersbox.resources.blacklisted
import com.programmersbox.resources.central_app_name
import com.programmersbox.resources.civitai_logo
import com.programmersbox.resources.exit
import com.programmersbox.resources.favorites
import com.programmersbox.resources.file
import com.programmersbox.resources.home
import com.programmersbox.resources.search
import com.programmersbox.resources.settings
import com.programmersbox.resources.stats
import com.programmersbox.resources.view
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
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
                            singleOf(::getDatabaseBuilder)
                            singleOf(::TrayState)
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
                    tooltip = stringResource(Res.string.central_app_name),
                    menu = {
                        Item(
                            stringResource(Res.string.home),
                            onClick = {
                                navHandler.backStack.clear()
                                navHandler.backStack.add(Screen.List)
                            },
                            enabled = navHandler.backStack.lastOrNull() != Screen.List,
                        )
                        Item(
                            stringResource(Res.string.favorites),
                            onClick = { navHandler.backStack.add(Screen.Favorites) },
                            enabled = navHandler.backStack.lastOrNull() != Screen.Favorites,
                        )
                        Item(
                            stringResource(Res.string.stats),
                            onClick = { navHandler.backStack.add(Screen.Settings.Stats) },
                            enabled = navHandler.backStack.lastOrNull() != Screen.Settings.Stats,
                        )
                        Item(
                            stringResource(Res.string.blacklisted),
                            onClick = { navHandler.backStack.add(Screen.Settings.Blacklisted) },
                            enabled = navHandler.backStack.lastOrNull() != Screen.Settings.Blacklisted,
                        )
                        Item(
                            stringResource(Res.string.settings),
                            onClick = { navHandler.backStack.add(Screen.Settings) },
                            enabled = navHandler.backStack.lastOrNull() != Screen.Settings,
                        )
                        Separator()
                        Item(
                            stringResource(Res.string.exit),
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
                            Menu(stringResource(Res.string.file)) {
                                Item(
                                    stringResource(DesktopResources.string.new_stats_window),
                                    onClick = { appState.openNewWindow(Screen.Settings.Stats) }
                                )
                                Item(
                                    stringResource(DesktopResources.string.new_backup_window),
                                    onClick = { appState.openNewWindow(Screen.Settings.Backup) }
                                )
                                Item(
                                    stringResource(DesktopResources.string.new_restore_window),
                                    onClick = { appState.openNewWindow(Screen.Settings.Restore) }
                                )
                                Item(
                                    stringResource(DesktopResources.string.new_about_window),
                                    onClick = { appState.openNewWindow(Screen.Settings.About) }
                                )
                                Menu(stringResource(Res.string.settings)) {
                                    Item(
                                        stringResource(DesktopResources.string.new_nsfw_settings_window),
                                        onClick = { appState.openNewWindow(Screen.Settings.Nsfw) }
                                    )
                                    Item(
                                        stringResource(DesktopResources.string.new_behavior_settings_window),
                                        onClick = { appState.openNewWindow(Screen.Settings.Behavior) }
                                    )
                                }
                                Separator()
                                Item(
                                    stringResource(Res.string.exit),
                                    onClick = ::exitApplication
                                )
                            }
                            Menu(stringResource(Res.string.view)) {
                                Item(
                                    stringResource(Res.string.home),
                                    onClick = {
                                        navHandler.backStack.clear()
                                        navHandler.backStack.add(Screen.List)
                                    },
                                    enabled = navHandler.backStack.lastOrNull() != Screen.List,
                                )
                                Item(
                                    stringResource(Res.string.favorites),
                                    onClick = { navHandler.backStack.add(Screen.Favorites) },
                                    enabled = navHandler.backStack.lastOrNull() != Screen.Favorites,
                                )
                                Item(
                                    stringResource(Res.string.search),
                                    onClick = { navHandler.backStack.add(Screen.Search) },
                                    enabled = navHandler.backStack.lastOrNull() != Screen.Search,
                                )
                                Item(
                                    stringResource(Res.string.stats),
                                    onClick = { navHandler.backStack.add(Screen.Settings.Stats) },
                                    enabled = navHandler.backStack.lastOrNull() != Screen.Settings.Stats,
                                )
                                Item(
                                    stringResource(Res.string.blacklisted),
                                    onClick = { navHandler.backStack.add(Screen.Settings.Blacklisted) },
                                    enabled = navHandler.backStack.lastOrNull() != Screen.Settings.Blacklisted,
                                )
                                Item(
                                    stringResource(Res.string.settings),
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