package com.programmersbox.common

import androidx.compose.foundation.lazy.grid.LazyGridState
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import com.programmersbox.common.db.BlacklistedItemRoom
import org.koin.core.module.Module
import kotlin.jvm.JvmInline

public expect fun getPlatformName(): String

internal expect val showRefreshButton: Boolean

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal expect fun SheetDetails(
    onDismiss: () -> Unit,
    content: @Composable () -> Unit,
)

@Composable
internal expect fun CustomScrollBar(
    lazyGridState: LazyGridState,
    modifier: Modifier = Modifier
)

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

@Composable
internal expect fun ContextMenuHandle(
    scope: ContextMenuScope.() -> Unit,
    content: @Composable () -> Unit,
)

interface ContextMenuScope {
    fun item(title: String, onClick: () -> Unit)
}

data class ContextMenuItems(
    val title: String,
    val onClick: () -> Unit,
)

internal class ContextMenuScopeImpl : ContextMenuScope {
    val items = mutableStateListOf<ContextMenuItems>()

    override fun item(title: String, onClick: () -> Unit) {
        items.add(ContextMenuItems(title, onClick))
    }
}

expect class DownloadHandler {
    fun download(url: String, name: String)
}

expect fun createPlatformModule(): Module

@JvmInline
value class ApplicationInfo(
    val versionName: String
)

fun isAmoledMode(
    colorScheme: ColorScheme,
    isDarkMode: Boolean,
    isAmoled: Boolean,
): ColorScheme = colorScheme.copy(
    background = if (isAmoled && isDarkMode) Color.Black else colorScheme.background,
    onBackground = if (isAmoled && isDarkMode) Color.White else colorScheme.onBackground,
    surface = if (isAmoled && isDarkMode) Color.Black else colorScheme.surface,
    onSurface = if (isAmoled && isDarkMode) Color.White else colorScheme.onSurface,
)