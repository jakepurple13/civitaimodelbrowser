package com.programmersbox.common

import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.lazy.grid.LazyGridState
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.room.Room
import androidx.room.RoomDatabase
import com.programmersbox.common.db.AppDatabase
import com.programmersbox.common.db.BlacklistedItemRoom
import com.programmersbox.common.presentation.backup.BackupRestoreHandler
import com.programmersbox.common.presentation.components.ToastType
import com.programmersbox.common.presentation.components.ToasterState
import com.tweener.alarmee.configuration.AlarmeeIosPlatformConfiguration
import com.tweener.alarmee.createAlarmeeService
import com.tweener.alarmee.model.Alarmee
import com.tweener.alarmee.model.AndroidNotificationConfiguration
import com.tweener.alarmee.model.IosNotificationConfiguration
import dev.icerock.moko.permissions.DeniedAlwaysException
import dev.icerock.moko.permissions.DeniedException
import dev.icerock.moko.permissions.Permission
import dev.icerock.moko.permissions.ios.PermissionsController
import dev.icerock.moko.permissions.notifications.REMOTE_NOTIFICATION
import io.github.vinceglb.filekit.FileKit
import io.github.vinceglb.filekit.dialogs.openFileSaver
import io.github.vinceglb.filekit.write
import io.ktor.client.plugins.onDownload
import io.ktor.client.request.get
import io.ktor.client.statement.bodyAsBytes
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import org.koin.core.module.Module
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.module
import platform.Foundation.NSDocumentDirectory
import platform.Foundation.NSFileManager
import platform.Foundation.NSUserDomainMask
import platform.UIKit.UIDevice
import kotlin.time.measureTime

@OptIn(markerClass = [androidx.compose.material3.ExperimentalMaterial3Api::class])
@Composable
internal actual fun SheetDetails(
    onDismiss: () -> Unit,
    content: @Composable (() -> Unit)
) {
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        contentWindowInsets = { WindowInsets.systemBars.only(WindowInsetsSides.Top) },
        sheetState = rememberModalBottomSheetState(
            skipPartiallyExpanded = true
        )
    ) {
        content()
    }
}

actual fun createPlatformModule(): Module = module {
    singleOf(::DownloadHandler)
    singleOf(::BackupRestoreHandler)
    singleOf(::NotificationHandler)
}

actual class DownloadHandler(
    private val network: Network,
    private val toasterState: ToasterState,
    private val notificationHandler: NotificationHandler
) {
    private val scope = CoroutineScope(Dispatchers.IO + Job())

    actual fun download(url: String, name: String) {
        scope.launch {
            runCatching {
                val extension = name.split(".").last()
                val file = FileKit.openFileSaver(
                    suggestedName = name.removeSuffix(".$extension"),
                    extension = extension,
                )
                if (file != null) {
                    measureTime {
                        file.write(
                            network
                                .client
                                .get(url) {
                                    onDownload { bytesSentTotal, contentLength ->
                                        val percentage =
                                            (bytesSentTotal.toDouble() / (contentLength
                                                ?: 1L) * 100)
                                                .toInt()
                                        println("Downloaded $percentage% $bytesSentTotal of $contentLength")
                                    }
                                }
                                .bodyAsBytes()
                        )
                    }
                } else {
                    null
                }
            }
                .onSuccess {
                    if (it != null) {
                        toasterState.show(
                            message = "Download Complete",
                            type = ToastType.Success
                        )
                        notificationHandler.notify(
                            title = "Download Complete",
                            message = "Downloaded $name in $it",
                            uuid = "download_completed",
                        )
                    } else {
                        toasterState.show(
                            message = "Download Failed",
                            type = ToastType.Error
                        )
                        notificationHandler.notify(
                            title = "Download Failed",
                            message = "Failed to download $name",
                            uuid = "download_failed",
                        )
                    }
                }
                .onFailure {
                    toasterState.show(
                        message = "Download Failed",
                        type = ToastType.Error
                    )
                    notificationHandler.notify(
                        title = "Download Failed",
                        message = "Failed to download $name",
                        uuid = "download_failed",
                    )
                    it.printStackTrace()
                }
        }
    }
}

@Composable
internal actual fun ContextMenu(
    isBlacklisted: Boolean,
    blacklistItems: List<BlacklistedItemRoom>,
    modelId: Long,
    name: String,
    nsfw: Boolean,
    imageUrl: String?,
    content: @Composable (() -> Unit)
) {
    content()
}

@Composable
internal actual fun ContextMenuHandle(
    scope: ContextMenuScope.() -> Unit,
    content: @Composable () -> Unit,
) {
    content()
}

@Composable
internal actual fun CustomScrollBar(
    lazyGridState: LazyGridState,
    modifier: Modifier
) {
}

internal actual val showRefreshButton: Boolean = false
actual fun getPlatformName(): String =
    UIDevice.currentDevice.systemName() + " " + UIDevice.currentDevice.systemVersion

fun getDatabaseBuilder(): RoomDatabase.Builder<AppDatabase> {
    val dbFilePath = documentDirectory() + "/civit.db"
    return Room.databaseBuilder<AppDatabase>(
        name = dbFilePath,
    )
}

@OptIn(ExperimentalForeignApi::class)
private fun documentDirectory(): String {
    val documentDirectory = NSFileManager.defaultManager.URLForDirectory(
        directory = NSDocumentDirectory,
        inDomain = NSUserDomainMask,
        appropriateForURL = null,
        create = false,
        error = null,
    )
    return requireNotNull(documentDirectory?.path)
}

class NotificationHandler {
    private val alarmeeService = createAlarmeeService()
    private val permissionsHandler = PermissionsController()
    private val scope = CoroutineScope(Dispatchers.IO + Job())

    init {
        alarmeeService.initialize(platformConfiguration = AlarmeeIosPlatformConfiguration)
    }

    fun notify(
        title: String,
        message: String,
        uuid: String,
        imageUrl: String? = null,
    ) {
        try {
            scope.launch {
                permissionsHandler.providePermission(
                    permission = Permission.REMOTE_NOTIFICATION
                )
                alarmeeService.local.immediate(
                    Alarmee(
                        uuid = uuid,
                        notificationTitle = title,
                        notificationBody = message,
                        imageUrl = imageUrl,
                        androidNotificationConfiguration = AndroidNotificationConfiguration(),
                        iosNotificationConfiguration = IosNotificationConfiguration(),
                    )
                )
            }
        } catch (deniedAlways: DeniedAlwaysException) {
            // Permission is always denied.
            deniedAlways.printStackTrace()
        } catch (denied: DeniedException) {
            // Permission was denied.
            denied.printStackTrace()
        }
    }
}