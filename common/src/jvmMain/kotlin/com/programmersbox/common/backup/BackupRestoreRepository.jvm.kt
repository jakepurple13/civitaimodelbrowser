package com.programmersbox.common.backup

import io.github.vinceglb.filekit.PlatformFile
import io.github.vinceglb.filekit.absolutePath
import kotlinx.coroutines.Dispatchers
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