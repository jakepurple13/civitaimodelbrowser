package com.programmersbox.common.backup

import com.oldguy.common.io.File
import com.oldguy.common.io.FileMode
import com.oldguy.common.io.ZipEntry
import com.oldguy.common.io.ZipFile
import io.github.vinceglb.filekit.PlatformFile
import io.github.vinceglb.filekit.absolutePath
import io.github.vinceglb.filekit.path
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okio.FileSystem
import okio.Path.Companion.toPath
import okio.buffer
import okio.openZip
import okio.use
import kotlin.time.measureTime

actual class Zipper {
    actual suspend fun zip(
        platformFile: PlatformFile,
        itemsToZip: Map<String, String>
    ) {
        //TODO: Need to get working
        ZipFile(
            File(platformFile.absolutePath()),
            FileMode.Write,
            zip64 = true
        ).use {
            itemsToZip.forEach { (name, handler) ->
                val duration = measureTime {
                    it.addEntry(
                        ZipEntry(nameArg = name)
                    ) { handler.encodeToByteArray() }
                }
                println("Zipped $name in $duration")
            }
        }
    }

    actual suspend fun unzip(
        platformFile: PlatformFile,
        onInfo: suspend (fileName: String, jsonString: String) -> Unit
    ) {
        withContext(Dispatchers.IO) {
            val zipFileSystem = FileSystem.SYSTEM.openZip(platformFile.path.toPath())
            val paths = zipFileSystem.listRecursively("/".toPath())
                .filter { zipFileSystem.metadata(it).isRegularFile }
                .toList()

            paths.forEach { zipFilePath ->
                zipFileSystem.source(zipFilePath).buffer().use { source ->
                    val duration = measureTime {
                        runCatching {
                            onInfo(
                                zipFilePath.toString().removePrefix("/"),
                                source.readUtf8()
                            )
                        }.onFailure { it.printStackTrace() }
                    }
                    println("Unzipped $zipFilePath in $duration")
                }
            }
        }
    }
}

actual class BackupRestoreHandler {
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
        }
    }
}