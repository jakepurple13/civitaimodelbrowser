package com.programmersbox.common.backup

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
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
import androidx.compose.material3.Card
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TriStateCheckbox
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
import androidx.compose.ui.state.ToggleableState
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import chaintech.videoplayer.ui.preview.VideoPreviewComposable
import com.programmersbox.common.ComposableUtils
import com.programmersbox.common.components.LoadingImage
import com.programmersbox.common.db.CustomList
import com.programmersbox.common.db.toImageHash

@Composable
fun BackupAndRestoreList(
    contentPadding: PaddingValues,
    includeFavorites: Boolean,
    includeBlacklisted: Boolean,
    includeSettings: Boolean,
    headline: String,
    listDialogTitle: String,
    listsToInclude: List<String>,
    lists: List<CustomList>?,
    addList: (String) -> Unit,
    removeList: (String) -> Unit,
    error: Throwable?,
    onIncludeFavorites: (Boolean) -> Unit,
    onIncludeBlacklisted: (Boolean) -> Unit,
    onIncludeSettings: (Boolean) -> Unit,
    includeSearchHistory: Boolean,
    onIncludeSearchHistory: (Boolean) -> Unit,
    searchHistoryCount: Int?,
    favoritesCount: Int?,
    blacklistedCount: Int?,
    settingsExtraContent: @Composable ColumnScope.() -> Unit = {},
) {
    LazyColumn(
        contentPadding = contentPadding,
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        item(
            contentType = "title"
        ) {
            Text(
                headline,
                style = MaterialTheme.typography.titleLarge,
                modifier = Modifier.padding(8.dp)
            )
        }
        favoritesCount?.let {
            item(
                contentType = "favorites"
            ) {
                FavoriteSwitch(
                    checked = includeFavorites,
                    onCheckedChange = onIncludeFavorites,
                    favoriteCount = it,
                    modifier = Modifier.padding(horizontal = 8.dp)
                )
            }
        }
        blacklistedCount?.let {
            item(
                contentType = "blacklisted"
            ) {
                BlacklistedSwitch(
                    checked = includeBlacklisted,
                    blacklistedCount = it,
                    onCheckedChange = onIncludeBlacklisted,
                    modifier = Modifier.padding(horizontal = 8.dp)
                )
            }
        }
        searchHistoryCount?.let {
            item(
                contentType = "search history"
            ) {
                SearchHistorySwitch(
                    checked = includeSearchHistory,
                    onCheckedChange = onIncludeSearchHistory,
                    searchHistoryCount = it,
                    modifier = Modifier.padding(horizontal = 8.dp)
                )
            }
        }
        item(
            contentType = "settings"
        ) {
            SettingsSwitch(
                checked = includeSettings,
                onCheckedChange = onIncludeSettings,
                supportingContent = settingsExtraContent,
                modifier = Modifier.padding(horizontal = 8.dp)
            )
        }
        lists?.let {
            item(
                contentType = "lists"
            ) {
                ListsToInclude(
                    title = listDialogTitle,
                    list = it,
                    listsToInclude = listsToInclude,
                    onAddList = addList,
                    onRemoveList = removeList,
                    modifier = Modifier.padding(horizontal = 8.dp)
                )
            }
        }
        item(
            contentType = "error"
        ) {
            AnimatedVisibility(error != null) {
                Column(
                    verticalArrangement = Arrangement.spacedBy(4.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    HorizontalDivider()
                    Text(
                        error?.message.orEmpty(),
                        modifier = Modifier.padding(16.dp)
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
    modifier: Modifier = Modifier,
) {
    Card(
        onClick = { onCheckedChange(!checked) },
        modifier = modifier
    ) {
        ListItem(
            headlineContent = { Text("Favorites ($favoriteCount)") },
            trailingContent = {
                Switch(
                    checked = checked,
                    onCheckedChange = onCheckedChange
                )
            },
            supportingContent = { Text("Favorite models, creators, and images") },
            colors = ListItemDefaults.colors(
                containerColor = Color.Transparent
            )
        )
    }
}

@Composable
private fun SearchHistorySwitch(
    searchHistoryCount: Int,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
) {
    Card(
        onClick = { onCheckedChange(!checked) },
        modifier = modifier
    ) {
        ListItem(
            headlineContent = { Text("Search History ($searchHistoryCount)") },
            trailingContent = {
                Switch(
                    checked = checked,
                    onCheckedChange = onCheckedChange
                )
            },
            supportingContent = { Text("All search queries") },
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
    modifier: Modifier = Modifier,
) {
    Card(
        onClick = { onCheckedChange(!checked) },
        modifier = modifier
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
    supportingContent: @Composable ColumnScope.() -> Unit,
    modifier: Modifier = Modifier,
) {
    Card(
        onClick = { onCheckedChange(!checked) },
        modifier = modifier
    ) {
        ListItem(
            headlineContent = { Text("App Settings") },
            trailingContent = {
                Switch(
                    checked = checked,
                    onCheckedChange = onCheckedChange
                )
            },
            supportingContent = {
                Column {
                    Text("App settings like theme, etc.")
                    supportingContent()
                }
            },
            colors = ListItemDefaults.colors(
                containerColor = Color.Transparent
            )
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ListsToInclude(
    title: String,
    list: List<CustomList>,
    listsToInclude: List<String>,
    onAddList: (String) -> Unit,
    onRemoveList: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    var showAdd by remember { mutableStateOf(false) }

    if (showAdd) {
        ModalBottomSheet(
            onDismissRequest = { showAdd = false },
            containerColor = MaterialTheme.colorScheme.surface,
        ) {
            LazyColumn {
                item(
                    contentType = "title"
                ) {
                    TopAppBar(
                        title = { Text(title) },
                        actions = {
                            IconButton(
                                onClick = { showAdd = false }
                            ) { Icon(Icons.Default.Close, null) }
                        }
                    )
                }
                item(
                    contentType = "add all"
                ) {
                    Card(
                        onClick = {
                            if (listsToInclude.size == list.size) {
                                list.forEach { onRemoveList(it.item.uuid) }
                            } else {
                                list.forEach { onAddList(it.item.uuid) }
                            }
                        }
                    ) {
                        ListItem(
                            headlineContent = { Text("Select All") },
                            trailingContent = {
                                TriStateCheckbox(
                                    state = when {
                                        listsToInclude.size == list.size -> ToggleableState.On
                                        listsToInclude.isEmpty() -> ToggleableState.Off
                                        else -> ToggleableState.Indeterminate
                                    },
                                    onClick = {
                                        if (listsToInclude.size == list.size) {
                                            list.forEach { onRemoveList(it.item.uuid) }
                                        } else {
                                            list.forEach { onAddList(it.item.uuid) }
                                        }
                                    }
                                )
                            }
                        )
                    }
                }
                items(
                    list,
                    contentType = { "list" },
                    key = { it.item.uuid }
                ) { customList ->
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
        modifier = modifier.animateContentSize()
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