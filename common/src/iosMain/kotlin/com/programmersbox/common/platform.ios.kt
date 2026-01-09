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
import kotlinx.cinterop.ExperimentalForeignApi
import org.koin.core.module.Module
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.module
import platform.Foundation.NSDocumentDirectory
import platform.Foundation.NSFileManager
import platform.Foundation.NSUserDomainMask
import platform.UIKit.UIDevice

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
}

actual class DownloadHandler {
    actual fun download(url: String, name: String) {
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