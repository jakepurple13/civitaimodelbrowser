package com.programmersbox.common.backup

import com.oldguy.common.io.File
import com.oldguy.common.io.FileMode
import com.oldguy.common.io.ZipEntry
import com.oldguy.common.io.ZipFile
import io.github.vinceglb.filekit.PlatformFile
import io.github.vinceglb.filekit.absolutePath
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlin.time.measureTime

//TODO: Need to get working
actual class Zipper {
    actual suspend fun zip(
        platformFile: PlatformFile,
        itemsToZip: Map<String, String>
    ) {
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
        ZipFile(
            File(platformFile.absolutePath()),
            FileMode.Read,
            zip64 = true
        ).use { zipFile ->
            zipFile.entries.forEach { entry ->
                zipFile.readEntry(entry) { d, data, _, _ ->
                    val duration = measureTime {
                        runCatching {
                            onInfo(
                                d.name,
                                data.decodeToString()
                            )
                        }
                    }
                    println("Unzipped ${d.name} in $duration")
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