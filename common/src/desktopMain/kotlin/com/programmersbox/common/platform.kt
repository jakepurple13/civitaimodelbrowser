package com.programmersbox.common

import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.window.Window

public actual fun getPlatformName(): String {
    return "civitaimodelbrowser"
}

@Composable
public fun UIShow(
    onShareClick: (String) -> Unit,
    producePath: () -> String,
) {
    App(onShareClick, producePath)
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