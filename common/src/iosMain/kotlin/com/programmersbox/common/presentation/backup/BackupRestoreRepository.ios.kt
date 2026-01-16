package com.programmersbox.common.presentation.backup

import com.programmersbox.common.NotificationHandler
import com.programmersbox.common.RestoreWorker
import com.programmersbox.common.presentation.components.ToastType
import com.programmersbox.common.presentation.components.ToasterState
import dev.brewkits.kmpworkmanager.background.domain.BackgroundTaskScheduler
import dev.brewkits.kmpworkmanager.background.domain.Constraints
import dev.brewkits.kmpworkmanager.background.domain.ExistingPolicy
import dev.brewkits.kmpworkmanager.background.domain.Qos
import dev.brewkits.kmpworkmanager.background.domain.TaskTrigger
import dev.brewkits.kmpworkmanager.background.domain.enqueue
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
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import nl.adaptivity.xmlutil.core.impl.multiplatform.name
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
import platform.Foundation.writeToFile
import kotlin.time.measureTime

actual class Zipper {
    @OptIn(ExperimentalForeignApi::class, BetaInteropApi::class)
    actual suspend fun zip(
        platformFile: PlatformFile,
        itemsToZip: Map<String, String>
    ) {
        withContext(Dispatchers.IO) {
            // We create the scope here so it lives through the entire operation
            memScoped {
                saveMultipleJsonsToZip(
                    jsonMap = itemsToZip,
                    destinationUrl = platformFile.nsUrl,
                    scope = this
                )
            }
        }
    }

    @OptIn(ExperimentalForeignApi::class, BetaInteropApi::class)
    private fun saveMultipleJsonsToZip(
        jsonMap: Map<String, String>,
        destinationUrl: NSURL,
        scope: MemScope
    ) {
        val fileManager = NSFileManager.defaultManager

        // 1. Prepare Staging Area
        // Use NSTemporaryDirectory() safely. Note: It usually includes the trailing slash.
        val stagingPath = NSTemporaryDirectory() + "civitai_backup_stage"

        // Clean previous staging if exists
        if (fileManager.fileExistsAtPath(stagingPath)) {
            val removeError = scope.alloc<ObjCObjectVar<NSError?>>()
            fileManager.removeItemAtPath(stagingPath, removeError.ptr)
        }

        // Create fresh directory
        // attributes: null means default permissions
        if (!fileManager.createDirectoryAtPath(stagingPath, true, null, null)) {
            println("❌ Failed to create staging directory")
            return
        }

        // 2. Write JSONs to Staging
        jsonMap.forEach { (name, content) ->
            // Ensure name ends in .json
            val safeName = if (name.endsWith(".json")) name else "$name.json"
            val filePath = "$stagingPath/$safeName"

            (content as NSString).writeToFile(
                path = filePath,
                atomically = true,
                encoding = NSUTF8StringEncoding,
                error = null
            )
        }

        // 3. Coordinate Zipping
        val stagingURL = NSURL.fileURLWithPath(stagingPath)
        val coordinator = NSFileCoordinator()

        // Allocate error pointer for the COORDINATOR itself
        val coordinatorError = scope.alloc<ObjCObjectVar<NSError?>>()

        var securityScoped = false

        try {
            // CRITICAL: If this URL is from a picker, we MUST unlock it
            securityScoped = destinationUrl.startAccessingSecurityScopedResource()

            coordinator.coordinateReadingItemAtURL(
                url = stagingURL,
                options = NSFileCoordinatorReadingForUploading, // This triggers the Zip creation
                error = coordinatorError.ptr // <--- FIX: Capture why it fails
            ) { tempZipUrl ->

                if (tempZipUrl != null) {
                    // Remove existing file at destination to prevent move failure
                    if (fileManager.fileExistsAtPath(destinationUrl.path!!)) {
                        fileManager.removeItemAtURL(destinationUrl, null)
                    }

                    val moveError = scope.alloc<ObjCObjectVar<NSError?>>()

                    val success = fileManager.moveItemAtURL(
                        srcURL = tempZipUrl,
                        toURL = destinationUrl,
                        error = moveError.ptr
                    )

                    if (success) {
                        println("✅ ZIP SUCCESS at: ${destinationUrl.path}")
                    } else {
                        val msg = moveError.value?.localizedDescription ?: "Unknown Move Error"
                        println("❌ ZIP MOVE FAILED: $msg")
                    }
                } else {
                    // If tempZipUrl is null, look at the coordinatorError
                    val msg =
                        coordinatorError.value?.localizedDescription ?: "Unknown Coordinator Error"
                    println("❌ ZIP COORDINATION FAILED: $msg")
                }
            }
        } finally {
            // CRITICAL: Always release the lock
            if (securityScoped) {
                destinationUrl.stopAccessingSecurityScopedResource()
            }

            // Clean up staging
            fileManager.removeItemAtPath(stagingPath, null)
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
        // 1. Get the path from your platform object
        // If platformFile holds an NSURL, use .path!! to get the string
        val zipPath = platformFile.path.toPath()
        println("Zip path: $zipPath")

        // 2. Open the file as a ZIP FileSystem
        // Okio treats the ZIP file almost like a folder you can navigate
        val zipFileSystem = FileSystem.SYSTEM.openZip(zipPath)

        try {
            // 3. List all files in the root of the zip (or recursively if needed)
            // "." represents the root of the zip archive
            val entries = zipFileSystem
                .listRecursively("/".toPath())
                .toList()

            for (entryPath in entries) {
                // Check metadata to ensure it's a file, not a directory
                val metadata = zipFileSystem.metadata(entryPath)
                println("File: $entryPath")
                println("Name: ${entryPath.name}")
                if (metadata.isRegularFile) {

                    // 4. Read the file content
                    zipFileSystem.source(entryPath).buffer().use { bufferedSource ->
                        val content = bufferedSource.readUtf8()

                        // 5. Invoke your callback
                        // entryPath.name gives "file.json"
                        val duration = measureTime {
                            onInfo(
                                entryPath.name,
                                content
                            )
                        }
                        println("Unzipped ${entryPath.name} in $duration")
                    }
                }
            }
        } catch (e: Exception) {
            // Handle Zip format errors or IO exceptions here
            println("Error reading zip: ${e.message}")
            throw e
        } finally {
            // 6. Crucial: Close the ZipFileSystem to release file handles
            // We can't use .use {} on the FileSystem itself in older Okio versions,
            // but explicit close is safe.
            try {
                zipFileSystem.close()
            } catch (e: Exception) {
                println("Error closing zip: ${e.message}")
            }
        }
    }
}

actual class BackupRestoreHandler(
    private val toasterState: ToasterState,
    private val notificationHandler: NotificationHandler,
    private val scheduler: BackgroundTaskScheduler
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
            println("Enqueueing restore")
            runCatching {
                scheduler.enqueue(
                    "restoring-task",
                    trigger = TaskTrigger.OneTime(),
                    workerClassName = RestoreWorker::class.name,
                    policy = ExistingPolicy.REPLACE,
                    constraints = Constraints(
                        qos = Qos.UserInitiated
                    ),
                    inputJson = Json.encodeToString(
                        RestoreInfo(
                            platformFile = platformFile,
                            includeFavorites = includeFavorites,
                            includeBlacklisted = includeBlacklisted,
                            includeSettings = includeSettings,
                            includeSearchHistory = includeSearchHistory,
                            //listItemsByUuid = listItemsByUuid
                        )
                    )
                )
            }.onFailure { it.printStackTrace() }
            /*val duration = measureTime {
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
            notificationHandler.notify(
                title = "Restore Complete",
                message = "Restore Complete in $duration",
                uuid = "restore_complete",
            )*/
        }
    }
}

@Serializable
data class RestoreInfo(
    val platformFile: PlatformFile,
    val includeFavorites: Boolean,
    val includeBlacklisted: Boolean,
    val includeSettings: Boolean,
    val includeSearchHistory: Boolean,
    //val listItemsByUuid: List<String>,
)