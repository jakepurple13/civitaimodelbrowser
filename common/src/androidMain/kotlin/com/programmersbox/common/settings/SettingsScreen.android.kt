package com.programmersbox.common.settings

import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Cached
import androidx.compose.material3.Card
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import com.programmersbox.common.components.ToastType
import com.programmersbox.common.components.ToasterState
import org.koin.compose.koinInject

@Composable
actual fun ColumnScope.ExtraSettings() {
    val context = LocalContext.current
    val snackBarState = koinInject<ToasterState>()

    HorizontalDivider()

    Card(
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
        }
    ) {
        ListItem(
            headlineContent = { Text("Clear Cache") },
            leadingContent = { Icon(Icons.Default.Cached, null) }
        )
    }
}