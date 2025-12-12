import androidx.compose.runtime.Composable
import androidx.compose.ui.window.AwtWindow
import androidx.compose.ui.window.Tray
import androidx.compose.ui.window.TrayState
import androidx.compose.ui.window.application
import ca.gosyer.appdirs.AppDirs
import com.dokar.sonner.Toaster
import com.dokar.sonner.ToasterState
import com.programmersbox.common.ApplicationInfo
import com.programmersbox.common.Network
import com.programmersbox.common.UIShow
import com.programmersbox.common.backup.Zipper
import com.programmersbox.common.cmpModules
import com.programmersbox.common.createPlatformModule
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
import java.awt.FileDialog
import java.awt.Frame
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
                val toaster = koinInject<ToasterState>()
                Tray(
                    state = koinInject<TrayState>(),
                    icon = painterResource(Res.drawable.civitai_logo),
                    menu = {
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

enum class FileDialogMode(internal val id: Int) { Load(FileDialog.LOAD), Save(FileDialog.SAVE) }

@Composable
private fun FileDialog(
    mode: FileDialogMode,
    title: String = "Choose a file",
    parent: Frame? = null,
    block: FileDialog.() -> Unit = {},
    onCloseRequest: (result: String?) -> Unit,
) = AwtWindow(
    create = {
        object : FileDialog(parent, title, mode.id) {
            override fun setVisible(value: Boolean) {
                super.setVisible(value)
                if (value) {
                    onCloseRequest(directory + File.separator + file)
                }
            }
        }.apply(block)
    },
    dispose = FileDialog::dispose
)

fun main1(): Unit = runBlocking {
    val n = Network()
    /*n.getModels(1)
        .onSuccess { println(it) }
        .onFailure { it.printStackTrace() }*/
    n.fetchModel("369730")
        .onSuccess { println(it) }
        .onFailure { it.printStackTrace() }
}