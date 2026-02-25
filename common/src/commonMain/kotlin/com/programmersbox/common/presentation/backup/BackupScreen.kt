package com.programmersbox.common.presentation.backup

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Backup
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularWavyProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.programmersbox.common.BackButton
import io.github.vinceglb.filekit.dialogs.FileKitDialogSettings
import io.github.vinceglb.filekit.dialogs.compose.rememberFileSaverLauncher
import org.koin.compose.viewmodel.koinViewModel

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun BackupScreen(
    viewModel: BackupViewModel = koinViewModel(),
) {
    val backupFile = rememberFileSaverLauncher(
        dialogSettings = FileKitDialogSettings.createDefault()
    ) {
        it?.let { platformFile -> viewModel.backup(platformFile) }
    }

    val backupItems by viewModel.backupItems.collectAsStateWithLifecycle()
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    if (uiState.isBackingUp) {
        AlertDialog(
            onDismissRequest = { },
            title = { Text("Backing Up") },
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
                title = { Text("Backup Data") },
                navigationIcon = { BackButton() }
            )
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                text = { Text("Backup") },
                icon = { Icon(Icons.Default.Backup, null) },
                onClick = { backupFile.launch("civitai_backup", "zip") }
            )
        }
    ) { padding ->
        BackupAndRestoreList(
            contentPadding = padding,
            includeFavorites = backupItems.includeFavorites,
            includeBlacklisted = backupItems.includeBlacklisted,
            includeSettings = backupItems.includeSettings,
            includeSearchHistory = backupItems.includeSearchHistory,
            headline = "Include in Backup",
            listDialogTitle = "Select Lists to Backup",
            listsToInclude = backupItems.listsToInclude,
            lists = viewModel
                .lists
                .collectAsStateWithLifecycle(emptyList())
                .value,
            addList = viewModel::addList,
            removeList = viewModel::removeList,
            error = uiState.error,
            onIncludeFavorites = viewModel::includeFavorites,
            onIncludeBlacklisted = viewModel::includeBlacklisted,
            onIncludeSettings = viewModel::includeSettings,
            onIncludeSearchHistory = viewModel::includeSearchHistory,
            searchHistoryCount = viewModel
                .searchHistoryCount
                .collectAsStateWithLifecycle(0)
                .value,
            favoritesCount = viewModel
                .favoritesCount
                .collectAsStateWithLifecycle(0)
                .value,
            blacklistedCount = viewModel
                .blacklistedCount
                .collectAsStateWithLifecycle(0)
                .value,
        )
    }
}