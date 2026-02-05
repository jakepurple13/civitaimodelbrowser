package com.programmersbox.common.presentation.settings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Computer
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SegmentedListItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import com.programmersbox.common.di.NavigationHandler
import org.koin.compose.koinInject

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
actual fun ColumnScope.ExtraSettings() {
    val colors =
        ListItemDefaults.colors(containerColor = MaterialTheme.colorScheme.surfaceContainer)

    val navigationHandler = koinInject<NavigationHandler>()

    Column(
        verticalArrangement = Arrangement.spacedBy(ListItemDefaults.SegmentedGap),
    ) {
        SegmentedListItem(
            content = { Text("Platform Settings") },
            leadingContent = { Icon(Icons.Default.Computer, null) },
            onClick = { navigationHandler.backStack.add(JvmSettingsScreen) },
            shapes = ListItemDefaults.segmentedShapes(0, 1),
            colors = colors
        )
    }
}