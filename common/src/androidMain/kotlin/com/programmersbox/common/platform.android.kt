package com.programmersbox.common

import android.app.Activity
import android.app.DownloadManager
import android.content.Context
import android.os.Environment
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.lazy.grid.LazyGridState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.material3.windowsizeclass.ExperimentalMaterial3WindowSizeClassApi
import androidx.compose.material3.windowsizeclass.calculateWindowSizeClass
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Modifier
import androidx.core.content.getSystemService
import androidx.core.net.toUri
import androidx.room.Room
import androidx.room.RoomDatabase
import com.programmersbox.common.db.AppDatabase
import com.programmersbox.common.db.BlacklistedItemRoom
import com.programmersbox.common.presentation.backup.BackupRestoreHandler
import org.koin.core.module.Module
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.module

actual class DownloadHandler(
    context: Context,
) {
    private val downloadManager = context.getSystemService<DownloadManager>()
    actual fun download(url: String, name: String) {
        val uri = url.toUri()
        val request = DownloadManager.Request(uri)
            .setTitle(name) // Title shown in the notification
            .setDescription("Downloading $name") // Description in the notification
            .setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED) // Show notification during and after download
            .setAllowedNetworkTypes(DownloadManager.Request.NETWORK_WIFI or DownloadManager.Request.NETWORK_MOBILE) // Allow download over Wi-Fi and mobile data
            .setDestinationInExternalPublicDir(
                Environment.DIRECTORY_DOWNLOADS,
                name
            ) // Save to public Downloads directory

        downloadManager?.enqueue(request)
    }
}

actual fun createPlatformModule(): Module = module {
    singleOf(::DownloadHandler)
    single { getDatabaseBuilder(get()) }
    singleOf(::BackupRestoreHandler)
}

public actual fun getPlatformName(): String {
    return "Android ${android.os.Build.VERSION.SDK_INT}"
}

@OptIn(ExperimentalMaterial3WindowSizeClassApi::class)
@Composable
public fun UIShow(
    onShareClick: (String) -> Unit,
    activity: Activity
) {
    CompositionLocalProvider(
        LocalWindowClassSize provides calculateWindowSizeClass(activity).widthSizeClass
    ) {
        App(
            onShareClick = onShareClick,
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal actual fun SheetDetails(
    onDismiss: () -> Unit,
    content: @Composable () -> Unit,
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

internal actual val showRefreshButton: Boolean = false

@Composable
internal actual fun CustomScrollBar(
    lazyGridState: LazyGridState,
    modifier: Modifier
) {

}

@Composable
internal actual fun ContextMenu(
    isBlacklisted: Boolean,
    blacklistItems: List<BlacklistedItemRoom>,
    modelId: Long,
    name: String,
    nsfw: Boolean,
    imageUrl: String?,
    content: @Composable () -> Unit,
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

fun getDatabaseBuilder(ctx: Context): RoomDatabase.Builder<AppDatabase> {
    val appContext = ctx.applicationContext
    val dbFile = appContext.getDatabasePath("my_room.db")
    return Room.databaseBuilder<AppDatabase>(
        context = appContext,
        name = dbFile.absolutePath
    )
}

/*
actual fun createRoomDatabase(): AppDatabase {
    val dbFile = getDatabasePath(dbFileName)
    return Room.databaseBuilder<AppDatabase>(
        context = app,
        name = dbFile.absolutePath,
    )
        .setDriver(BundledSQLiteDriver())
        .setQueryCoroutineContext(Dispatchers.IO)
        .build()
}*/
