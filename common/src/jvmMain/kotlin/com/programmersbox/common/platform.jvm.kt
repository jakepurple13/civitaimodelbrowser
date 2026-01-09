package com.programmersbox.common

import androidx.compose.foundation.ContextMenuArea
import androidx.compose.foundation.ContextMenuItem
import androidx.compose.foundation.VerticalScrollbar
import androidx.compose.foundation.lazy.grid.LazyGridState
import androidx.compose.foundation.rememberScrollbarAdapter
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.window.Notification
import androidx.compose.ui.window.TrayState
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.room.Room
import androidx.room.RoomDatabase
import ca.gosyer.appdirs.AppDirs
import com.programmersbox.common.db.AppDatabase
import com.programmersbox.common.db.BlacklistedItemRoom
import com.programmersbox.common.db.FavoritesDao
import com.programmersbox.common.presentation.backup.BackupRestoreHandler
import io.ktor.client.call.body
import io.ktor.client.plugins.onDownload
import io.ktor.client.request.get
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import org.koin.compose.koinInject
import org.koin.core.module.Module
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.module
import java.io.File

actual class DownloadHandler(
    private val network: Network,
    private val dataStoreHandler: DataStoreHandler,
    private val trayState: TrayState
) {
    private val scope = CoroutineScope(Dispatchers.IO + Job())
    actual fun download(url: String, name: String) {
        scope.launch {
            val parent = File(dataStoreHandler.downloadPath.get())
            if (!parent.exists()) parent.mkdirs()

            val file = File(parent, name)
            if (!file.exists()) file.createNewFile()

            file.writeBytes(
                network
                    .client
                    .get(url) {
                        onDownload { bytesSentTotal, contentLength ->
                            val percentage =
                                (bytesSentTotal.toDouble() / (contentLength ?: 1L) * 100)
                                    .toInt()
                            println("Downloaded $percentage% $bytesSentTotal of $contentLength")
                        }
                    }
                    .body()
            )

            trayState.sendNotification(
                Notification(
                    title = "Download Complete",
                    message = "Downloaded $name",
                    type = Notification.Type.Info
                )
            )
        }
    }
}

actual fun createPlatformModule(): Module = module {
    singleOf(::DownloadHandler)
    singleOf(::DataStoreHandler)
    singleOf(::BackupRestoreHandler)
}

class DataStoreHandler(
    private val dataStore: DataStore
) {
    val downloadPath = DataStore.DataStoreTypeNonNull(
        key = stringPreferencesKey("download_path"),
        dataStore = dataStore.dataStore,
        defaultValue = System.getProperty("user.home") + File.separator + "Downloads" + File.separator + "CivitAi"
    )

    @Composable
    fun rememberDownloadPath() = rememberPreference(
        downloadPath.key,
        downloadPath.defaultValue
    )

    @Composable
    private fun <T> rememberPreference(
        key: Preferences.Key<T>,
        defaultValue: T,
    ): MutableState<T> {
        val coroutineScope = rememberCoroutineScope()
        val state by remember {
            dataStore.dataStore.data.map { it[key] ?: defaultValue }
        }.collectAsStateWithLifecycle(initialValue = defaultValue)

        return remember(state) {
            object : MutableState<T> {
                override var value: T
                    get() = state
                    set(value) {
                        coroutineScope.launch {
                            dataStore.dataStore.edit { it[key] = value }
                        }
                    }

                override fun component1() = value
                override fun component2(): (T) -> Unit = { value = it }
            }
        }
    }
}

public actual fun getPlatformName(): String {
    return "Jvm ${System.getProperty("java.version")}"
}

@Composable
public fun UIShow(
    onShareClick: (String) -> Unit,
    producePath: () -> String,
) {
    App(
        onShareClick = onShareClick,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal actual fun SheetDetails(
    onDismiss: () -> Unit,
    content: @Composable () -> Unit,
) {
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(
            skipPartiallyExpanded = true
        )
    ) {
        content()
    }
}

internal actual val showRefreshButton: Boolean = true


@Composable
internal actual fun CustomScrollBar(
    lazyGridState: LazyGridState,
    modifier: Modifier
) {
    VerticalScrollbar(
        adapter = rememberScrollbarAdapter(lazyGridState),
        modifier = modifier
    )
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
    val scope = rememberCoroutineScope()
    val db = koinInject<FavoritesDao>()
    ContextMenuArea(
        items = {
            listOfNotNull(
                if (isBlacklisted) {
                    ContextMenuItem("Remove from Blacklist") {
                        scope.launch {
                            blacklistItems.find { b -> b.id == modelId }
                                ?.let { db.delete(it) }
                        }
                    }
                } else {
                    ContextMenuItem("Add to Blacklist") {
                        scope.launch {
                            db.blacklistItem(modelId, name, nsfw, imageUrl)
                        }
                    }
                }
            )
        },
        content = content
    )
}

@Composable
internal actual fun ContextMenuHandle(
    scope: ContextMenuScope.() -> Unit,
    content: @Composable () -> Unit,
) {
    val contextMenuScope = remember(scope) { ContextMenuScopeImpl().apply(scope) }
    val items by remember {
        derivedStateOf {
            contextMenuScope
                .items
                .map { ContextMenuItem(it.title, it.onClick) }
        }
    }
    ContextMenuArea(
        items = { items },
        content = content
    )
}

fun getDatabaseBuilder(
    appDirs: AppDirs
): RoomDatabase.Builder<AppDatabase> {
    val dbFile = File(
        appDirs.getUserDataDir(),
        "my_room.db"
    )
    return Room.databaseBuilder<AppDatabase>(
        name = dbFile.absolutePath,
    )
}