package com.programmersbox.common.backup

import androidx.compose.ui.window.Notification
import androidx.compose.ui.window.TrayState
import com.dokar.sonner.ToastType
import com.dokar.sonner.ToasterState
import io.github.vinceglb.filekit.PlatformFile
import io.github.vinceglb.filekit.absolutePath
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.FileInputStream
import java.io.FileOutputStream
import java.util.zip.ZipEntry
import java.util.zip.ZipInputStream
import java.util.zip.ZipOutputStream
import kotlin.time.measureTime

actual class Zipper {
    actual suspend fun zip(
        platformFile: PlatformFile,
        itemsToZip: Map<String, String>
    ) {
        val f = platformFile.absolutePath()
        withContext(Dispatchers.IO) {
            ZipOutputStream(FileOutputStream(f)).use { zip ->
                itemsToZip.forEach { (name, handler) ->
                    val duration = measureTime {
                        zip.putNextEntry(ZipEntry(name))
                        runCatching { zip.write(handler.toByteArray()) }
                    }
                    println("Zipped $name in $duration")
                }
            }
        }
    }

    actual suspend fun unzip(
        platformFile: PlatformFile,
        onInfo: suspend (fileName: String, jsonString: String) -> Unit
    ) {
        withContext(Dispatchers.IO) {
            FileInputStream(platformFile.absolutePath()).use { inStream ->
                ZipInputStream(inStream).use { zipIs ->
                    var entry: ZipEntry?
                    while (true) {
                        entry = zipIs.nextEntry
                        if (entry == null) break
                        val duration = measureTime {
                            runCatching {
                                onInfo(
                                    entry.name,
                                    zipIs.bufferedReader().readText()
                                )
                            }
                        }
                        println("Unzipped ${entry.name} in $duration")
                    }
                }
            }
        }
    }
}

actual class BackupRestoreHandler(
    private val toasterState: ToasterState,
    private val trayState: TrayState
) {
    private val scope = CoroutineScope(Dispatchers.IO + Job())
    actual fun restore(
        backupRepository: BackupRepository,
        platformFile: PlatformFile,
        includeFavorites: Boolean,
        includeBlacklisted: Boolean,
        includeSettings: Boolean,
        includeSearchHistory: Boolean,
        listItemsByUuid: List<String>
    ) {
        scope.launch {
            val duration = measureTime {
                backupRepository.restoreItems(
                    backupItems = backupRepository.readItems(platformFile),
                    includeSettings = includeSettings,
                    includeFavorites = includeFavorites,
                    includeBlacklisted = includeBlacklisted,
                    includeSearchHistory = includeSearchHistory,
                )
            }
            println("Restored in $duration")
            toasterState.show(
                "Restore Complete in $duration",
                type = ToastType.Success
            )
            trayState.sendNotification(
                Notification(
                    title = "Restore Complete",
                    message = "Restore Complete in $duration",
                    type = Notification.Type.Info
                )
            )
        }
    }
}