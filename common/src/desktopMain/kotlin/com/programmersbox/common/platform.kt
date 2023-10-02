package com.programmersbox.common

import androidx.compose.runtime.Composable

public actual fun getPlatformName(): String {
    return "civitaimodelbrowser"
}

@Composable
public fun UIShow(
    onShareClick: (String) -> Unit,
) {
    App(onShareClick)
}