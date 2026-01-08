package com.programmersbox.common.backup

import io.github.vinceglb.filekit.PlatformFile
import io.github.vinceglb.filekit.path
import kotlinx.cinterop.BetaInteropApi
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.ObjCObjectVar
import kotlinx.cinterop.alloc
import kotlinx.cinterop.memScoped
import kotlinx.cinterop.ptr
import kotlinx.cinterop.value
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okio.FileSystem
import okio.Path
import okio.Path.Companion.toPath
import okio.buffer
import okio.openZip
import okio.use
import platform.Foundation.NSError
import platform.Foundation.NSFileCoordinator
import platform.Foundation.NSFileCoordinatorReadingForUploading
import platform.Foundation.NSFileManager
import platform.Foundation.NSString
import platform.Foundation.NSTemporaryDirectory
import platform.Foundation.NSURL
import platform.Foundation.NSUTF8StringEncoding
import platform.Foundation.create
import platform.Foundation.writeToFile
import platform.darwin.dispatch_async
import platform.darwin.dispatch_get_main_queue
import kotlin.time.measureTime

actual class Zipper {
    @OptIn(ExperimentalForeignApi::class, BetaInteropApi::class)
    actual suspend fun zip(
        platformFile: PlatformFile,
        itemsToZip: Map<String, String>
    ) {
        withContext(Dispatchers.IO) {
            saveMultipleJsonsToZip(
                jsonMap = itemsToZip,
                zipName = platformFile.nsUrl
            )
        }
    }

    @OptIn(ExperimentalForeignApi::class, BetaInteropApi::class)
    private fun saveMultipleJsonsToZip(jsonMap: Map<String, String>, zipName: NSURL) {
        val fileManager = NSFileManager.defaultManager

        // 1. Get the public Documents directory
        val finalZipURL = zipName

        // 2. Create a hidden staging area
        val stagingPath = NSTemporaryDirectory() + "civitai_backup"
        fileManager.createDirectoryAtPath(stagingPath, true, null, null)

        // 3. Write your JSON strings
        jsonMap.forEach { (name, content) ->
            val path = "$stagingPath/$name"
            NSString
                .create(content)
                ?.writeToFile(path, true, NSUTF8StringEncoding, null)
        }

        // 4. Perform the zip via Coordinator
        val stagingURL = NSURL.fileURLWithPath(stagingPath)
        val coordinator = NSFileCoordinator()

        val errorPtr = memScoped { alloc<ObjCObjectVar<NSError?>>() }

        coordinator.coordinateReadingItemAtURL(
            stagingURL,
            NSFileCoordinatorReadingForUploading,
            null
        ) { zipURL ->
            if (zipURL != null) {
                // Clean up old version if it exists
                if (fileManager.fileExistsAtPath(finalZipURL.path!!)) {
                    fileManager.removeItemAtURL(finalZipURL, null)
                }

                // Move the generated zip to the public Documents folder
                val success = fileManager.moveItemAtURL(
                    srcURL = zipURL,
                    toURL = finalZipURL,
                    error = errorPtr.ptr
                )

                dispatch_async(dispatch_get_main_queue()) {
                    if (success) {
                        println("✅ ZIP SUCCESS: ${finalZipURL.path}")
                        // Clean up staging
                        fileManager.removeItemAtPath(stagingPath, null)
                    } else {
                        println("❌ ZIP FAILED to move ${errorPtr.value?.localizedDescription ?: "Unknown error"}")
                        throw Exception(errorPtr.value?.localizedDescription)
                    }
                }
            }
        }
    }

    private fun getAllFilesFromFolder(
        list: MutableList<Path>,
        zipFileSystem: FileSystem,
        parentPath: Path
    ) {
        zipFileSystem.listRecursively(parentPath).forEach { path ->
            println("Path: $path")
            if (zipFileSystem.metadata(path).isRegularFile) {
                list.add(path)
                println("Added $path")
            } else {
                println("Not a file $path")
                getAllFilesFromFolder(list, zipFileSystem, path)
            }
        }
    }

    actual suspend fun unzip(
        platformFile: PlatformFile,
        onInfo: suspend (fileName: String, jsonString: String) -> Unit
    ) {
        withContext(Dispatchers.IO) {
            val zipFileSystem = FileSystem.SYSTEM.openZip(platformFile.path.toPath())
            val paths = mutableListOf<Path>()
            getAllFilesFromFolder(paths, zipFileSystem, "/".toPath())

            paths.forEach { zipFilePath ->
                zipFileSystem.source(zipFilePath).buffer().use { source ->
                    val duration = measureTime {
                        runCatching {
                            onInfo(
                                zipFilePath.name,
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