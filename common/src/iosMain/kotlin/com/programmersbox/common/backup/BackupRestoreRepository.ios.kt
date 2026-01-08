package com.programmersbox.common.backup

import io.github.vinceglb.filekit.PlatformFile
import io.github.vinceglb.filekit.path
import kotlinx.cinterop.BetaInteropApi
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.MemScope
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
import kotlin.time.measureTime

actual class Zipper {
    @OptIn(ExperimentalForeignApi::class, BetaInteropApi::class)
    actual suspend fun zip(
        platformFile: PlatformFile,
        itemsToZip: Map<String, String>
    ) {
        // 1. Move memScoped to the top level so pointers stay valid
        // 2. Run on IO context
        withContext(Dispatchers.IO) {
            memScoped {
                saveMultipleJsonsToZip(
                    jsonMap = itemsToZip,
                    zipName = platformFile.nsUrl,
                    scope = this // Pass the memory scope if needed, or just use memScoped inside the helper
                )
            }
        }
    }

    @OptIn(ExperimentalForeignApi::class, BetaInteropApi::class)
    private fun saveMultipleJsonsToZip(
        jsonMap: Map<String, String>,
        zipName: NSURL,
        scope: MemScope // Keeping it explicitly scoped is safer
    ) {
        val fileManager = NSFileManager.defaultManager
        val finalZipURL = zipName

        // 1. Define Staging Path
        val stagingPath = NSTemporaryDirectory() + "civitai_backup"

        // 🧹 CRITICAL: Clean staging area FIRST to prevent "Zips inside Zips" or stale files
        if (fileManager.fileExistsAtPath(stagingPath)) {
            val error = scope.alloc<ObjCObjectVar<NSError?>>()
            fileManager.removeItemAtPath(stagingPath, error.ptr)
        }

        // 2. Create fresh directory
        fileManager.createDirectoryAtPath(stagingPath, true, null, null)

        // 3. Write JSON strings
        jsonMap.forEach { (name, content) ->
            val filePath = "$stagingPath/$name"
            NSString
                .create(content)
                ?.writeToFile(
                    path = filePath,
                    atomically = true,
                    encoding = NSUTF8StringEncoding,
                    error = null
                )
        }

        // 4. Prepare for Zipping
        val stagingURL = NSURL.fileURLWithPath(stagingPath)
        val coordinator = NSFileCoordinator()

        // Allocate the error pointer inside the valid scope passed from above
        val errorPtr = scope.alloc<ObjCObjectVar<NSError?>>()

        // Note: This block is synchronous, so we are safe on threading
        coordinator.coordinateReadingItemAtURL(
            stagingURL,
            NSFileCoordinatorReadingForUploading,
            null
        ) { zipURL ->
            if (zipURL != null) {
                // Clean up old zip at destination if it exists
                if (fileManager.fileExistsAtPath(finalZipURL.path!!)) {
                    fileManager.removeItemAtURL(finalZipURL, null)
                }

                // 5. Move the generated zip to the public Documents folder
                val success = fileManager.moveItemAtURL(
                    srcURL = zipURL,
                    toURL = finalZipURL,
                    error = errorPtr.ptr // Pointer is now valid!
                )

                if (success) {
                    println("✅ ZIP SUCCESS: ${finalZipURL.path}")
                    // Clean up staging folder to keep device clean
                    fileManager.removeItemAtPath(stagingPath, null)
                } else {
                    val errorMsg = errorPtr.value?.localizedDescription ?: "Unknown error"
                    println("❌ ZIP FAILED to move: $errorMsg")
                    // Throwing here might crash the app if not caught in UI layer, logging is safer
                }
            } else {
                println("❌ ZIP FAILED: Coordinator returned null URL")
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
            val zipPath = platformFile.path.toPath()

            // Safety check: Ensure zip exists
            if (!FileSystem.SYSTEM.exists(zipPath)) {
                println("❌ Zip file not found: $zipPath")
                return@withContext
            }

            FileSystem.SYSTEM.openZip(zipPath).use { zipFileSystem ->

                // 1. Get all files
                val paths = zipFileSystem.listRecursively("/".toPath())
                    .filter { path ->
                        val metadata = zipFileSystem.metadata(path)
                        val name = path.name

                        // 🛡️ SECURITY & STABILITY FILTER 🛡️
                        metadata.isRegularFile &&       // Must be a file
                                !name.startsWith(".") &&        // Ignore .DS_Store, .gitignore
                                !name.startsWith("__MACOSX") // Ignore Mac metadata folders
                    }
                    .toList()

                paths.forEach { zipFilePath ->
                    zipFileSystem.source(zipFilePath).buffer().use { source ->
                        runCatching {
                            // 2. Read safely
                            val content = source.readUtf8()

                            // 3. Pass to callback (ensure this doesn't block if onInfo touches UI)
                            val duration = measureTime {
                                onInfo(
                                    zipFilePath.name.split("/").last(),
                                    content
                                )
                            }
                            println("Unzipped ${zipFilePath.name} in $duration")
                        }.onFailure {
                            println("❌ Failed to process ${zipFilePath.name}: ${it.message}")
                            it.printStackTrace()
                        }
                    }
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