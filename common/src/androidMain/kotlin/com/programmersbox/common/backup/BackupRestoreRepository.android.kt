package com.programmersbox.common.backup

import android.content.Context
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.workDataOf
import com.programmersbox.common.RestoreWorker
import com.programmersbox.common.logToFirebase
import io.github.vinceglb.filekit.PlatformFile
import io.github.vinceglb.filekit.dialogs.toAndroidUri
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.FileInputStream
import java.io.FileOutputStream
import java.util.zip.ZipEntry
import java.util.zip.ZipInputStream
import java.util.zip.ZipOutputStream
import kotlin.time.measureTime

actual class Zipper(
    private val context: Context,
) {
    actual suspend fun zip(
        platformFile: PlatformFile,
        itemsToZip: Map<String, String>
    ) {
        val f = platformFile.toAndroidUri(context.applicationContext.packageName + ".fileprovider")

        withContext(Dispatchers.IO) {
            // 1. Open the PFD in a 'use' block so it ALWAYS closes correctly
            context.contentResolver.openFileDescriptor(f, "wt")?.use { pfd ->
                // 2. Create the streams
                ZipOutputStream(FileOutputStream(pfd.fileDescriptor)).use { zip ->
                    itemsToZip.forEach { (name, handler) ->
                        val duration = measureTime {
                            zip.putNextEntry(ZipEntry(name))
                            // Note: runCatching here might hide errors that should fail the whole zip
                            zip.write(handler.toByteArray())
                            zip.closeEntry()
                        }
                        logToFirebase("Zipped $name in $duration")
                    }
                    // 3. Ensure zip finishes writing its footer/central directory
                    zip.finish()
                }
            }
        }
    }

    actual suspend fun unzip(
        platformFile: PlatformFile,
        onInfo: suspend (fileName: String, jsonString: String) -> Unit
    ) {
        withContext(Dispatchers.IO) {
            val pfd = context
                .contentResolver
                .openFileDescriptor(
                    platformFile
                        .toAndroidUri(
                            context.applicationContext.packageName + ".fileprovider"
                        ),
                    "r"
                )!!
            pfd.use {
                FileInputStream(it.fileDescriptor).use { inStream ->
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
                                }.onFailure { it.printStackTrace() }
                            }
                            logToFirebase("Unzipped ${entry.name} in $duration")
                        }
                    }
                }
            }
        }
    }
}

actual class BackupRestoreHandler(
    private val context: Context
) {
    actual fun restore(
        backupRepository: BackupRepository,
        platformFile: PlatformFile,
        includeFavorites: Boolean,
        includeBlacklisted: Boolean,
        includeSettings: Boolean,
        includeSearchHistory: Boolean,
        listItemsByUuid: List<String>
    ) {
        WorkManager
            .getInstance(context)
            .enqueueUniqueWork(
                "RestoreWorker",
                ExistingWorkPolicy.REPLACE,
                OneTimeWorkRequestBuilder<RestoreWorker>()
                    .setInputData(
                        workDataOf(
                            "file" to platformFile
                                .toAndroidUri(context.applicationContext.packageName + ".fileprovider")
                                .toString(),
                            "includeFavorites" to includeFavorites,
                            "includeBlacklisted" to includeBlacklisted,
                            "includeSettings" to includeSettings,
                            "includeSearchHistory" to includeSearchHistory,
                            "listsToInclude" to listItemsByUuid.toTypedArray()
                        )
                    )
                    .build()
            )
    }
}

data class ApplicationIcon(val icon: Int)
