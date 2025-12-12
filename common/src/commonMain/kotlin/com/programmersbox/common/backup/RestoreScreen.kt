package com.programmersbox.common.backup

import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.ImportExport
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CircularWavyProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import chaintech.videoplayer.ui.preview.VideoPreviewComposable
import com.programmersbox.common.BackButton
import com.programmersbox.common.ComposableUtils
import com.programmersbox.common.components.LoadingImage
import com.programmersbox.common.db.CustomList
import com.programmersbox.common.db.toImageHash
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
    ) {
        it?.let { platformFile -> viewModel.read(platformFile) }
    }

    LaunchedEffect(Unit) {
        restoreFile.launch()
    }

    if (viewModel.isRestoring) {
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

    if (viewModel.isReading) {
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
                title = { Text("Restore") },
                navigationIcon = { BackButton() }
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { viewModel.restore() }
            ) { Icon(Icons.Default.ImportExport, null) }
        }
    ) { padding ->
        LazyColumn(
            contentPadding = padding,
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            viewModel.backupItems?.let { backupItems ->
                item {
                    FavoriteSwitch(
                        checked = viewModel.includeFavorites,
                        onCheckedChange = { viewModel.includeFavorites = it },
                        favoriteCount = backupItems.favorites?.size ?: 0
                    )
                }
                item {
                    BlacklistedSwitch(
                        checked = viewModel.includeBlacklisted,
                        blacklistedCount = backupItems.blacklisted?.size ?: 0,
                        onCheckedChange = { viewModel.includeBlacklisted = it }
                    )
                }
                item {
                    SettingsSwitch(
                        backupSettings = backupItems.settings,
                        checked = viewModel.includeSettings,
                        onCheckedChange = { viewModel.includeSettings = it },
                    )
                }

                item {
                    ListsToInclude(
                        list = backupItems.lists.orEmpty(),
                        listsToInclude = viewModel.listsToInclude,
                        onAddList = viewModel::addList,
                        onRemoveList = viewModel::removeList
                    )
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
            headlineContent = { Text("Include Favorites ($favoriteCount)") },
            trailingContent = {
                Switch(
                    checked = checked,
                    onCheckedChange = onCheckedChange
                )
            }
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
            headlineContent = { Text("Include Blacklisted ($blacklistedCount)") },
            trailingContent = {
                Switch(
                    checked = checked,
                    onCheckedChange = onCheckedChange
                )
            }
        )
    }
}

@Composable
private fun SettingsSwitch(
    backupSettings: BackupSettings?,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
) {
    Card(
        onClick = { onCheckedChange(!checked) },
    ) {
        ListItem(
            headlineContent = { Text("Include Settings") },
            trailingContent = {
                Switch(
                    checked = checked,
                    onCheckedChange = onCheckedChange
                )
            },
            supportingContent = {
                Column {
                    backupSettings?.let { settings ->
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
            }
        )
    }
}

@Composable
private fun ListsToInclude(
    list: List<CustomList>,
    listsToInclude: List<String>,
    onAddList: (String) -> Unit,
    onRemoveList: (String) -> Unit,
) {
    var showAdd by remember { mutableStateOf(false) }
    Card(
        modifier = Modifier.animateContentSize()
    ) {
        ListItem(
            headlineContent = { Text("Include Lists (${listsToInclude.size})") },
            trailingContent = {
                IconButton(
                    onClick = { showAdd = !showAdd }
                ) {
                    Icon(
                        Icons.Default.ArrowDropDown,
                        null,
                        modifier = Modifier.rotate(
                            animateFloatAsState(if (showAdd) 180f else 0f).value
                        )
                    )
                }
            },
            supportingContent = {
                if (showAdd) {
                    Column {
                        list.forEach { customList ->
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
        )
    }
}

@Composable
private fun ListItemToAdd(
    customList: CustomList,
    listsToInclude: List<String>,
    onAddList: (String) -> Unit,
    onRemoveList: (String) -> Unit,
) {
    Card(
        onClick = {
            if (customList.item.uuid in listsToInclude) {
                onRemoveList(customList.item.uuid)
            } else {
                onAddList(customList.item.uuid)
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
                    checked = customList.item.uuid in listsToInclude,
                    onCheckedChange = {
                        if (it) {
                            onAddList(customList.item.uuid)
                        } else {
                            onRemoveList(customList.item.uuid)
                        }
                    }
                )
            },
            leadingContent = {
                val imageHashing = customList.toImageHash()

                val imageModifier = Modifier
                    .size(ComposableUtils.IMAGE_WIDTH / 3, ComposableUtils.IMAGE_HEIGHT / 3)
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