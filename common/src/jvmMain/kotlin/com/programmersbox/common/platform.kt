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
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.room.Room
import androidx.room.RoomDatabase
import ca.gosyer.appdirs.AppDirs
import com.programmersbox.common.db.AppDatabase
import com.programmersbox.common.db.BlacklistedItemRoom
import com.programmersbox.common.db.FavoritesDao
import kotlinx.coroutines.launch
import org.koin.compose.koinInject
import java.io.File

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

internal actual fun getPagingPlaceholderKey(index: Int): Any = index

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
internal actual fun CustomScrollBar(lazyGridState: LazyGridState, modifier: Modifier) {
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