package com.programmersbox.common.backup

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Restore
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.CircularWavyProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.programmersbox.common.BackButton
import io.github.vinceglb.filekit.dialogs.FileKitType
import io.github.vinceglb.filekit.dialogs.compose.rememberFilePickerLauncher
import org.koin.compose.viewmodel.koinViewModel

@OptIn(ExperimentalMaterial3ExpressiveApi::class, ExperimentalMaterial3Api::class)
@Composable
fun RestoreScreen(
    viewModel: RestoreViewModel = koinViewModel(),
) {
    val restoreFile = rememberFilePickerLauncher(
        type = FileKitType.File("zip"),
    ) { it?.let { platformFile -> viewModel.read(platformFile) } }

    LaunchedEffect(Unit) {
        restoreFile.launch()
    }

    if (viewModel.uiState.isRestoring) {
        AlertDialog(
            onDismissRequest = { },
            title = { Text("Restoring") },
            text = {
                Column(
                    verticalArrangement = Arrangement.spacedBy(4.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    CircularWavyProgressIndicator()
                    Text("Please wait...")
                }
            },
            confirmButton = { }
        )
    }

    if (viewModel.uiState.isReading) {
        AlertDialog(
            onDismissRequest = { },
            title = { Text("Reading") },
            text = {
                Column(
                    verticalArrangement = Arrangement.spacedBy(4.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    CircularWavyProgressIndicator()
                    Text("Please wait...")
                }
            },
            confirmButton = { }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Restore Backup") },
                navigationIcon = { BackButton() }
            )
        },
        floatingActionButton = {
            AnimatedVisibility(
                visible = viewModel.backupItems != null,
                enter = fadeIn() + slideInHorizontally { it },
                exit = slideOutHorizontally { it } + fadeOut()
            ) {
                ExtendedFloatingActionButton(
                    icon = { Icon(Icons.Default.Restore, null) },
                    text = { Text("Restore") },
                    onClick = viewModel::restore
                )
            }
        }
    ) { padding ->
        viewModel.backupItems?.let { backupItems ->
            BackupAndRestoreList(
                contentPadding = padding,
                includeFavorites = viewModel.includeFavorites,
                includeBlacklisted = viewModel.includeBlacklisted,
                includeSettings = viewModel.includeSettings,
                includeSearchHistory = viewModel.includeSearchHistory,
                headline = "Select items to Restore",
                listDialogTitle = "Select Lists to Restore",
                listsToInclude = viewModel.listsToInclude,
                lists = backupItems.lists.orEmpty(),
                addList = viewModel::addList,
                removeList = viewModel::removeList,
                error = viewModel.uiState.error,
                onIncludeFavorites = { viewModel.includeFavorites = it },
                onIncludeBlacklisted = { viewModel.includeBlacklisted = it },
                onIncludeSettings = { viewModel.includeSettings = it },
                onIncludeSearchHistory = { viewModel.includeSearchHistory = it },
                favoritesCount = backupItems.favorites?.size ?: 0,
                blacklistedCount = backupItems.blacklisted?.size ?: 0,
                searchHistoryCount = backupItems.searchHistory?.size ?: 0,
                settingsExtraContent = {
                    backupItems.settings?.let { settings ->
                        HorizontalDivider()
                        settings.stringSettings.forEach { (key, value) ->
                            Text("$key: $value")
                        }
                        settings.intSettings.forEach { (key, value) ->
                            Text("$key: $value")
                        }
                        settings.longSettings.forEach { (key, value) ->
                            Text("$key: $value")
                        }
                        settings.booleanSettings.forEach { (key, value) ->
                            Text("$key: $value")
                        }
                        settings.doubleSettings.forEach { (key, value) ->
                            Text("$key: $value")
                        }
                        settings.byteArraySettings.forEach { (key, value) ->
                            Text("$key: $value")
                        }
                    }
                }
            )
        } ?: run {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier.fillMaxSize()
            ) {
                Column(
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(
                        Icons.Default.Restore,
                        null,
                        modifier = Modifier.size(96.dp)
                    )
                    Button(
                        onClick = restoreFile::launch
                    ) { Text("Select File") }
                }
            }
        }
    }
}
