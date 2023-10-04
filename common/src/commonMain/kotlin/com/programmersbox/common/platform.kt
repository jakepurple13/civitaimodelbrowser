package com.programmersbox.common

import androidx.compose.runtime.Composable

public expect fun getPlatformName(): String

internal expect fun getPagingPlaceholderKey(index: Int): Any

internal expect val showRefreshButton: Boolean

@Composable
internal expect fun SheetDetails(
    sheetDetails: ModelImage,
    onDismiss: () -> Unit,
    content: @Composable (ModelImage) -> Unit,
)