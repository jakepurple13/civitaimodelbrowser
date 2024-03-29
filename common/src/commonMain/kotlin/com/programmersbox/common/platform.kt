package com.programmersbox.common

import androidx.compose.foundation.lazy.grid.LazyGridState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

public expect fun getPlatformName(): String

internal expect fun getPagingPlaceholderKey(index: Int): Any

internal expect val showRefreshButton: Boolean

@Composable
internal expect fun SheetDetails(
    onDismiss: () -> Unit,
    content: @Composable () -> Unit,
)

@Composable
internal expect fun CustomScrollBar(lazyGridState: LazyGridState, modifier: Modifier)