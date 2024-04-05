package com.programmersbox.common

import androidx.compose.foundation.ContextMenuArea
import androidx.compose.foundation.ContextMenuItem
import androidx.compose.foundation.VerticalScrollbar
import androidx.compose.foundation.lazy.grid.LazyGridState
import androidx.compose.foundation.rememberScrollbarAdapter
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.window.Window
import com.programmersbox.common.db.BlacklistedItem
import com.programmersbox.common.db.FavoriteModel
import kotlinx.coroutines.launch

public actual fun getPlatformName(): String {
    return "civitaimodelbrowser"
}

@Composable
public fun UIShow(
    onShareClick: (String) -> Unit,
    producePath: () -> String,
    onExport: (List<FavoriteModel>) -> Unit,
    onImport: () -> String,
    export: @Composable () -> Unit = {},
    import: @Composable () -> Unit = {},
) {
    App(
        onShareClick = onShareClick,
        producePath = producePath,
        onExport = onExport,
        onImport = onImport,
        export = export,
        import = import
    )
}

internal actual fun getPagingPlaceholderKey(index: Int): Any = index

@Composable
internal actual fun SheetDetails(
    onDismiss: () -> Unit,
    content: @Composable () -> Unit,
) {
    Window(
        onCloseRequest = onDismiss
    ) {
        Surface {
            content()
        }
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
    blacklistItems: List<BlacklistedItem>,
    modelId: Long,
    name: String,
    nsfw: Boolean,
    imageUrl: String?,
    content: @Composable () -> Unit,
) {
    val scope = rememberCoroutineScope()
    val db = LocalDatabase.current
    ContextMenuArea(
        items = {
            listOfNotNull(
                ContextMenuItem("Add to Blacklist") {
                    scope.launch {
                        db.blacklistItem(modelId, name, nsfw, imageUrl)
                    }
                }.takeIf { !isBlacklisted },
                ContextMenuItem("Remove from Blacklist") {
                    scope.launch {
                        blacklistItems.find { b -> b.id == modelId }
                            ?.let { db.removeBlacklistItem(it) }
                    }
                }.takeIf { isBlacklisted }
            )
        },
        content = content
    )
}