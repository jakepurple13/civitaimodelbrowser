package com.programmersbox.common

import androidx.compose.runtime.Composable

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
