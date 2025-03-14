package com.programmersbox.common

import androidx.compose.foundation.lazy.grid.LazyGridState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.programmersbox.common.db.BlacklistedItemRoom

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

@Composable
internal expect fun ContextMenu(
    isBlacklisted: Boolean,
    blacklistItems: List<BlacklistedItemRoom>,
    modelId: Long,
    name: String,
    nsfw: Boolean,
    imageUrl: String?,
    content: @Composable () -> Unit,
)