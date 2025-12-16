package com.programmersbox.common.backup

import android.content.Context
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
            val pfd = context
                .contentResolver
                .openFileDescriptor(f, "w")!!
            ZipOutputStream(FileOutputStream(pfd.fileDescriptor)).use { zip ->
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
                            println("Unzipped ${entry.name} in $duration")
                        }
                    }
                }
            }
        }
    }
}