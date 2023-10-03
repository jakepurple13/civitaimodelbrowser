package com.programmersbox.common

import androidx.compose.runtime.Composable
import androidx.compose.ui.window.DialogWindow

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
    sheetDetails: ModelImage,
    onDismiss: () -> Unit,
    content: @Composable (ModelImage) -> Unit,
) {
    DialogWindow(
        onCloseRequest = onDismiss
    ) {
        content(sheetDetails)
    }
    /*Window(
        onCloseRequest = onDismiss
    ) {
        content(sheetDetails)
    }*/
}