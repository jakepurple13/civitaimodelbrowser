import androidx.compose.material3.Card
import androidx.compose.material3.ListItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.window.AwtWindow
import androidx.compose.ui.window.Tray
import androidx.compose.ui.window.TrayState
import androidx.compose.ui.window.application
import ca.gosyer.appdirs.AppDirs
import com.dokar.sonner.Toast
import com.dokar.sonner.ToastType
import com.dokar.sonner.Toaster
import com.dokar.sonner.rememberToasterState
import com.programmersbox.common.LocalDatabaseDao
import com.programmersbox.common.Network
import com.programmersbox.common.UIShow
import com.programmersbox.common.cmpModules
import com.programmersbox.common.createPlatformModule
import com.programmersbox.common.getDatabaseBuilder
import com.programmersbox.resources.Res
import com.programmersbox.resources.civitai_logo
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.json.Json
import org.jetbrains.compose.resources.painterResource
import org.koin.compose.KoinApplication
import org.koin.compose.koinInject
import org.koin.dsl.module
import java.awt.FileDialog
import java.awt.Frame
import java.awt.Toolkit
import java.awt.datatransfer.DataFlavor
import java.awt.datatransfer.StringSelection
import java.awt.dnd.DnDConstants
import java.awt.dnd.DropTarget
import java.awt.dnd.DropTargetAdapter
import java.awt.dnd.DropTargetDragEvent
import java.awt.dnd.DropTargetDropEvent
import java.awt.dnd.DropTargetEvent
import java.io.File
import java.io.FileOutputStream
import kotlin.time.Duration

fun main() {
    System.setProperty("apple.awt.application.appearance", "system")
    application {
        val toaster = rememberToasterState()
        val scope = rememberCoroutineScope()


        KoinApplication(
            application = {
                modules(
                    cmpModules(),
                    createPlatformModule(),
                    module {
                        //single { producePath() }
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
                    }
                )
            }
        ) {
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
                onCloseRequest = ::exitApplication,
                frameWindowScope = {
                    var dragState by remember { mutableStateOf(false) }

                    LaunchedEffect(Unit) {
                        window.dropTarget = DropTarget().apply {
                            addDropTargetListener(
                                object : DropTargetAdapter() {
                                    override fun dragEnter(dtde: DropTargetDragEvent?) {
                                        super.dragEnter(dtde)
                                        dragState = true
                                        toaster.show(
                                            "Import file?",
                                            type = ToastType.Info,
                                            id = 13,
                                            duration = Duration.INFINITE
                                        )
                                    }

                                    override fun drop(event: DropTargetDropEvent) {
                                        event.acceptDrop(DnDConstants.ACTION_COPY)
                                        val draggedFileName =
                                            event.transferable.getTransferData(DataFlavor.javaFileListFlavor)
                                        println(draggedFileName)
                                        when (draggedFileName) {
                                            is List<*> -> {
                                                draggedFileName.firstOrNull()?.toString()?.let {
                                                    if (it.endsWith(".json")) {
                                                        toaster.dismiss(13)
                                                        it
                                                            .let { File(it).readText() }
                                                        //.let { scope.launch { db.import(it) } }

                                                        toaster.show(
                                                            Toast(
                                                                message = "Import Completed",
                                                                type = ToastType.Success
                                                            )
                                                        )
                                                    }
                                                }
                                            }
                                        }
                                        event.dropComplete(true)
                                        dragState = false
                                    }

                                    override fun dragExit(dte: DropTargetEvent?) {
                                        super.dragExit(dte)
                                        dragState = false
                                        toaster.dismiss(13)
                                    }
                                }
                            )
                        }
                    }
                }
            ) {
                var import by remember { mutableStateOf(false) }
                var export by remember { mutableStateOf(false) }

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
                    onExport = { export = true },
                    onImport = {
                        import = true
                        ""
                    },
                    export = {
                        val database = LocalDatabaseDao.current
                        if (export) {
                            FileDialog(
                                FileDialogMode.Save,
                                block = {
                                    setFilenameFilter { _, name -> name.endsWith(".json") }
                                    file = "civitbrowser.json"
                                }
                            ) { path ->
                                path?.let { filePath ->
                                    scope.launch {
                                        val json = Json {
                                            isLenient = true
                                            prettyPrint = true
                                            ignoreUnknownKeys = true
                                            coerceInputValues = true
                                        }
                                        val file = File(filePath)
                                        if (!file.exists()) file.createNewFile()
                                        FileOutputStream(file).use { f ->
                                            f.write(
                                                json.encodeToString(database.export()).toByteArray()
                                            )
                                        }
                                        toaster.show(
                                            Toast(
                                                message = "Export Completed",
                                                type = ToastType.Success
                                            )
                                        )
                                    }
                                }
                            }
                        }
                    },
                    import = {
                        val database = LocalDatabaseDao.current
                        if (import) {
                            FileDialog(
                                FileDialogMode.Load,
                                block = {
                                    setFilenameFilter { _, name -> name.endsWith(".json") }
                                    file = "civitbrowser.json"
                                }
                            ) { path ->
                                path
                                    ?.let { File(it).readText() }
                                    ?.let {
                                        scope.launch {
                                            database.importFavorites(it)
                                            toaster.show(
                                                Toast(
                                                    message = "Import Completed",
                                                    type = ToastType.Success
                                                )
                                            )
                                        }
                                    }
                            }
                        }

                        Card(
                            onClick = { import = true }
                        ) {
                            ListItem(
                                headlineContent = { Text("Import Favorites") }
                            )
                        }
                    }
                )

                Toaster(
                    state = toaster,
                    richColors = true
                )
            }
        }
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