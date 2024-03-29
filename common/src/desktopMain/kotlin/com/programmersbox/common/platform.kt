package com.programmersbox.common

import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.window.Window
import com.programmersbox.common.db.FavoriteModel

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