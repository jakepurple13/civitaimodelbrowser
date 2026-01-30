import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowRight
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.unit.dp
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
                var showNavTree by remember { mutableStateOf(false) }

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
                            "Show Navigation Tree",
                            onClick = { showNavTree = !showNavTree },
                            enabled = true
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
                    windowTitle = stringResource(Res.string.central_app_name),
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

                            Menu("Developer") {
                                Item(
                                    "Show Navigation Tree",
                                    onClick = { showNavTree = !showNavTree },
                                    enabled = true
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

                if (showNavTree) {
                    NavigationTree(
                        onCloseRequest = { showNavTree = false }
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
    windowTitle = state.destination.toString(),
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NavigationTree(
    onCloseRequest: () -> Unit,
) {
    val navTree = remember { createNavigationGraph() }
    val maxDepth = remember { findMaxDepth(navTree) }
    WindowWithBar(
        windowTitle = "Navigation Tree",
        onCloseRequest = onCloseRequest
    ) {
        Scaffold { padding ->
            Column(
                modifier = Modifier
                    .padding(padding)
                    .padding(horizontal = 16.dp)
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
            ) {
                MaxDepthInfoItem(maxDepth)
                HorizontalDivider()
                Graph(navTree)
            }
        }
    }
}

@Composable
private fun MaxDepthInfoItem(
    maxDepthInfo: MaxDepthInfo
) {
    Column {
        Text("Max Depth: ${maxDepthInfo.maxDepth}")
        Text("Path: ${maxDepthInfo.list.joinToString(" -> ") { it.toString() }}")
    }
}

@Composable
private fun Graph(
    node: NavNode,
) {
    Column {
        var showChildren by remember { mutableStateOf(false) }

        TextButton(
            onClick = { showChildren = !showChildren }
        ) {
            Icon(
                Icons.AutoMirrored.Filled.ArrowRight,
                null,
                modifier = Modifier.rotate(
                    animateFloatAsState(if (showChildren) 90f else 0f).value
                )
            )
            Text(node.key.toString())
            Text("(${node.children.size})")
        }

        AnimatedVisibility(showChildren) {
            Column(
                verticalArrangement = Arrangement.spacedBy(4.dp),
                modifier = Modifier.padding(start = 16.dp)
            ) {
                node.children.forEach { child ->
                    Graph(child)
                }
                HorizontalDivider()
            }
        }
    }
}

@Stable
data class NavNode(
    val key: NavKey,
    val children: MutableList<NavNode> = mutableListOf(),
) {
    fun add(child: NavNode) {
        children.add(child)
    }
}

@Stable
data class MaxDepthInfo(
    val maxDepth: Int,
    val list: List<NavKey>
)

fun findMaxDepth(
    node: NavNode,
    set: MutableSet<NavKey> = mutableSetOf()
): MaxDepthInfo {
    if (node.key in set) return MaxDepthInfo(node.children.size, node.children.map { it.key })
    set.add(node.key)
    val max = node
        .children
        .map { findMaxDepth(it, set) }
        .maxByOrNull { it.maxDepth }
        ?: MaxDepthInfo(0, emptyList())

    println("${node.key} -> $max")
    return MaxDepthInfo(
        1 + max.maxDepth,
        listOf(node.key) + max.list
    )
}

fun createNavigationGraph(): NavNode {
    val root = NavNode(Screen.List)
    val favorites = NavNode(Screen.Favorites)
    val settings = NavNode(Screen.Settings)
    val blacklisted = NavNode(Screen.Settings.Blacklisted)
    val nsfw = NavNode(Screen.Settings.Nsfw)
    val behavior = NavNode(Screen.Settings.Behavior)
    val backup = NavNode(Screen.Settings.Backup)
    val restore = NavNode(Screen.Settings.Restore)
    val stats = NavNode(Screen.Settings.Stats)
    val about = NavNode(Screen.Settings.About)
    val search = NavNode(Screen.Search)
    val user = NavNode(Screen.User("Test"))
    val details = NavNode(Screen.Detail("Test"))
    val images = NavNode(Screen.Images)
    val qrCode = NavNode(Screen.QrCode)
    val customList = NavNode(Screen.CustomList)
    val customListDetail = NavNode(Screen.CustomListDetail("Test"))
    val webView = NavNode(Screen.WebView("Test"))
    val onboarding = NavNode(Screen.Onboarding)
    val detailsImage = NavNode(Screen.DetailsImage("Test", "Test"))

    settings.add(nsfw)
    settings.add(behavior)
    settings.add(backup)
    settings.add(restore)
    settings.add(stats)
    settings.add(about)
    settings.add(onboarding)
    settings.add(webView)

    customList.add(customListDetail)
    customListDetail.add(details)
    customListDetail.add(user)

    details.add(detailsImage)
    details.add(user)

    user.add(details)

    search.add(details)
    search.add(user)
    search.add(detailsImage)

    qrCode.add(details)

    favorites.add(details)
    favorites.add(user)

    images.add(user)

    root.add(favorites)
    root.add(settings)
    root.add(search)
    root.add(user)
    root.add(details)
    root.add(images)
    root.add(qrCode)
    root.add(customList)
    root.add(blacklisted)

    return root
}