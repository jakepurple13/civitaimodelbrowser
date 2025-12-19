package com.programmersbox.common.backup

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CircularWavyProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import chaintech.videoplayer.ui.preview.VideoPreviewComposable
import com.programmersbox.common.BackButton
import com.programmersbox.common.ComposableUtils
import com.programmersbox.common.components.LoadingImage
import com.programmersbox.common.db.CustomList
import com.programmersbox.common.db.toImageHash
import io.github.vinceglb.filekit.dialogs.compose.rememberFileSaverLauncher
import org.koin.compose.viewmodel.koinViewModel

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun BackupScreen(
    viewModel: BackupViewModel = koinViewModel(),
) {
    val backupFile = rememberFileSaverLauncher {
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
            FloatingActionButton(
                onClick = { backupFile.launch("civitai_backup", "zip") }
            ) { Text("Backup") }
        }
    ) { padding ->
        LazyColumn(
            contentPadding = padding,
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            item {
                Text(
                    "Include in Backup",
                    style = MaterialTheme.typography.titleLarge,
                    modifier = Modifier.padding(8.dp)
                )
            }
            item {
                FavoriteSwitch(
                    checked = backupItems.includeFavorites,
                    onCheckedChange = viewModel::includeFavorites,
                    favoriteCount = viewModel
                        .favoritesCount
                        .collectAsStateWithLifecycle(0)
                        .value
                )
            }
            item {
                BlacklistedSwitch(
                    checked = backupItems.includeBlacklisted,
                    blacklistedCount = viewModel
                        .blacklistedCount
                        .collectAsStateWithLifecycle(0)
                        .value,
                    onCheckedChange = viewModel::includeBlacklisted
                )
            }
            item {
                SettingsSwitch(
                    checked = backupItems.includeSettings,
                    onCheckedChange = viewModel::includeSettings
                )
            }
            item {
                ListsToInclude(
                    list = viewModel
                        .lists
                        .collectAsStateWithLifecycle(emptyList())
                        .value,
                    listsToInclude = backupItems.listsToInclude,
                    onAddList = viewModel::addList,
                    onRemoveList = viewModel::removeList
                )
            }
            item {
                AnimatedVisibility(uiState.error != null) {
                    Column(
                        verticalArrangement = Arrangement.spacedBy(4.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        HorizontalDivider()
                        Text(
                            uiState.error?.message.orEmpty(),
                            modifier = Modifier.padding(16.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun FavoriteSwitch(
    favoriteCount: Int,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
) {
    Card(
        onClick = { onCheckedChange(!checked) }
    ) {
        ListItem(
            headlineContent = { Text("Favorites ($favoriteCount)") },
            trailingContent = {
                Switch(
                    checked = checked,
                    onCheckedChange = onCheckedChange
                )
            },
            supportingContent = { Text("Favorite models and images") },
            colors = ListItemDefaults.colors(
                containerColor = Color.Transparent
            )
        )
    }
}

@Composable
private fun BlacklistedSwitch(
    blacklistedCount: Int,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
) {
    Card(
        onClick = { onCheckedChange(!checked) }
    ) {
        ListItem(
            headlineContent = { Text("Blacklisted ($blacklistedCount)") },
            trailingContent = {
                Switch(
                    checked = checked,
                    onCheckedChange = onCheckedChange
                )
            },
            supportingContent = { Text("Blocked models and images") },
            colors = ListItemDefaults.colors(
                containerColor = Color.Transparent
            )
        )
    }
}

@Composable
private fun SettingsSwitch(
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
) {
    Card(
        onClick = { onCheckedChange(!checked) }
    ) {
        ListItem(
            headlineContent = { Text("App Settings") },
            trailingContent = {
                Switch(
                    checked = checked,
                    onCheckedChange = onCheckedChange
                )
            },
            supportingContent = { Text("App settings like theme, etc.") },
            colors = ListItemDefaults.colors(
                containerColor = Color.Transparent
            )
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ListsToInclude(
    list: List<CustomList>,
    listsToInclude: List<CustomList>,
    onAddList: (CustomList) -> Unit,
    onRemoveList: (CustomList) -> Unit,
) {
    var showAdd by remember { mutableStateOf(false) }

    if (showAdd) {
        ModalBottomSheet(
            onDismissRequest = { showAdd = false },
            containerColor = MaterialTheme.colorScheme.surface,
        ) {
            LazyColumn {
                item {
                    TopAppBar(
                        title = { Text("Select Lists to Backup") },
                        actions = {
                            IconButton(
                                onClick = { showAdd = false }
                            ) { Icon(Icons.Default.Close, null) }
                        }
                    )
                }
                items(list) { customList ->
                    ListItemToAdd(
                        customList = customList,
                        listsToInclude = listsToInclude,
                        onAddList = onAddList,
                        onRemoveList = onRemoveList
                    )
                }
            }
        }
    }

    Card(
        onClick = { showAdd = !showAdd },
        modifier = Modifier.animateContentSize()
    ) {
        ListItem(
            headlineContent = { Text("Select Lists (${listsToInclude.size} selected)") },
            trailingContent = { Icon(Icons.Default.ChevronRight, null) },
            colors = ListItemDefaults.colors(
                containerColor = Color.Transparent
            )
        )
    }
}

@Composable
private fun ListItemToAdd(
    customList: CustomList,
    listsToInclude: List<CustomList>,
    onAddList: (CustomList) -> Unit,
    onRemoveList: (CustomList) -> Unit,
) {
    Card(
        onClick = {
            if (listsToInclude.any { it.item.uuid == customList.item.uuid }) {
                onRemoveList(customList)
            } else {
                onAddList(customList)
            }
        }
    ) {
        ListItem(
            headlineContent = { Text(customList.item.name) },
            overlineContent = { Text("Items: ${customList.list.size}") },
            supportingContent = {
                Column {
                    customList.list.take(3).forEach { info ->
                        Text(
                            text = info.name,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            },
            trailingContent = {
                Checkbox(
                    checked = listsToInclude.any { it.item.uuid == customList.item.uuid },
                    onCheckedChange = {
                        if (it) {
                            onAddList(customList)
                        } else {
                            onRemoveList(customList)
                        }
                    }
                )
            },
            leadingContent = {
                val imageHashing = customList.toImageHash()

                val imageModifier = Modifier
                    .size(
                        ComposableUtils.IMAGE_WIDTH / 3,
                        ComposableUtils.IMAGE_HEIGHT / 3
                    )
                    .clip(MaterialTheme.shapes.medium)

                if (imageHashing?.url?.endsWith("mp4") == true) {
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .background(Color.Black)
                            .then(imageModifier)
                    ) {
                        VideoPreviewComposable(
                            url = imageHashing.url,
                            frameCount = 5,
                            contentScale = ContentScale.Crop,
                        )
                        Row(
                            verticalAlignment = Alignment.Bottom,
                            horizontalArrangement = Arrangement.SpaceEvenly,
                            modifier = Modifier.matchParentSize()
                        ) {
                            Text("Click to Play")
                            Icon(Icons.Default.PlayArrow, null)
                        }
                    }
                } else {
                    LoadingImage(
                        imageUrl = imageHashing?.url.orEmpty(),
                        isNsfw = customList.list.any { it.nsfw },
                        name = "",
                        hash = imageHashing?.hash,
                        modifier = imageModifier,
                    )
                }
            }
        )
    }
}