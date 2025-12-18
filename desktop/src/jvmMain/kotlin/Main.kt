import androidx.compose.ui.window.Tray
import androidx.compose.ui.window.TrayState
import androidx.compose.ui.window.application
import ca.gosyer.appdirs.AppDirs
import com.dokar.sonner.Toaster
import com.dokar.sonner.ToasterState
import com.programmersbox.common.ApplicationInfo
import com.programmersbox.common.NavigationHandler
import com.programmersbox.common.Network
import com.programmersbox.common.Screen
import com.programmersbox.common.UIShow
import com.programmersbox.common.backup.Zipper
import com.programmersbox.common.createPlatformModule
import com.programmersbox.common.di.cmpModules
import com.programmersbox.common.getDatabaseBuilder
import com.programmersbox.common.qrcode.QrCodeRepository
import com.programmersbox.desktop.BuildKonfig
import com.programmersbox.resources.Res
import com.programmersbox.resources.civitai_logo
import kotlinx.coroutines.runBlocking
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
                        Item(
                            "Exit",
                            onClick = { exitApplication() }
                        )
                    }
                )

                WindowWithBar(
                    onCloseRequest = ::exitApplication
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

fun main1(): Unit = runBlocking {
    val n = Network()
    /*n.getModels(1)
        .onSuccess { println(it) }
        .onFailure { it.printStackTrace() }*/
    n.fetchModel("369730")
        .onSuccess { println(it) }
        .onFailure { it.printStackTrace() }
}