package com.programmersbox.common

import androidx.compose.foundation.lazy.grid.LazyGridState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.programmersbox.common.db.BlacklistedItemRoom
import org.koin.core.module.Module

public expect fun getPlatformName(): String

internal expect val showRefreshButton: Boolean

@OptIn(ExperimentalMaterial3Api::class)
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

expect class DownloadHandler {
    fun download(url: String, name: String)
}

expect fun createPlatformModule(): Module

@JvmInline
value class ApplicationInfo(
    val versionName: String
)