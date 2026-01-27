package com.programmersbox.common.presentation.settings

import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Download
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import com.programmersbox.common.DataStoreHandler
import io.github.vinceglb.filekit.PlatformFile
import io.github.vinceglb.filekit.absolutePath
import io.github.vinceglb.filekit.dialogs.compose.rememberDirectoryPickerLauncher
import org.koin.compose.koinInject

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
actual fun ColumnScope.ExtraSettings() {
    var downloadPath by koinInject<DataStoreHandler>().rememberDownloadPath()
    val directoryPicker = rememberDirectoryPickerLauncher(
        directory = PlatformFile(downloadPath)
    ) { file -> file?.let { downloadPath = it.absolutePath() } }

    ListItem(
        content = { Text("Download Path") },
        supportingContent = { Text(downloadPath) },
        leadingContent = { Icon(Icons.Default.Download, null) },
        onClick = { directoryPicker.launch() },
        colors = ListItemDefaults.colors(containerColor = MaterialTheme.colorScheme.surfaceContainer)
    )
}