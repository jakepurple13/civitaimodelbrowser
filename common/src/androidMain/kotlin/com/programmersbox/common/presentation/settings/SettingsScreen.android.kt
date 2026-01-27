package com.programmersbox.common.presentation.settings

import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Cached
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import com.programmersbox.common.presentation.components.ToastType
import com.programmersbox.common.presentation.components.ToasterState
import org.koin.compose.koinInject

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
actual fun ColumnScope.ExtraSettings() {
    val context = LocalContext.current
    val snackBarState = koinInject<ToasterState>()

    ListItem(
        content = { Text("Clear Cache") },
        leadingContent = { Icon(Icons.Default.Cached, null) },
        onClick = {
            runCatching {
                context
                    .cacheDir
                    .listFiles()
                    ?.forEach {
                        if (it.name == "journal" || it.name.startsWith("journal.")) {
                            println("Skipping ${it.name}")
                            return@forEach
                        }
                        println("Deleting ${it.name}")
                        if (it.deleteRecursively()) {
                            println("Deleted ${it.name}")
                        } else {
                            println("Failed to delete ${it.name}")
                        }
                    }
            }
                .onSuccess { snackBarState.show("Cache Cleared", type = ToastType.Success) }
                .onFailure { snackBarState.show("Failed to Clear Cache", type = ToastType.Error) }
                .onFailure { it.printStackTrace() }
        },
        colors = ListItemDefaults.colors(containerColor = MaterialTheme.colorScheme.surfaceContainer)
    )
}