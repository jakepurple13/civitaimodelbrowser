package com.programmersbox.common.lists

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.RemoveCircle
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardColors
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SheetState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import chaintech.videoplayer.ui.preview.VideoPreviewComposable
import com.programmersbox.common.ComposableUtils
import com.programmersbox.common.DataStore
import com.programmersbox.common.components.LoadingImage
import com.programmersbox.common.db.CustomList
import com.programmersbox.common.db.toImageHash
import kotlinx.coroutines.launch
import org.koin.compose.koinInject
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun ListDetailScreen(
    viewModel: ListDetailViewModel = koinViewModel(),
) {
    val dataStore = koinInject<DataStore>()
    val showNsfw by remember { dataStore.showNsfw.flow }
        .collectAsStateWithLifecycle(false)
    val blurStrength by remember { dataStore.hideNsfwStrength.flow }
        .collectAsStateWithLifecycle(6f)


}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
private fun InfoSheet(
    customItem: CustomList,
    sheetState: SheetState,
    showNsfw: Boolean,
    blurStrength: Dp,
    rename: (String) -> Unit,
    onDismiss: () -> Unit,
    onDeleteListAction: () -> Unit,
    onRemoveItemsAction: () -> Unit,
) {
    val scope = rememberCoroutineScope()

    var currentName by remember { mutableStateOf(customItem.item.name) }

    var showAdd by remember { mutableStateOf(false) }

    if (showAdd) {
        AlertDialog(
            onDismissRequest = { showAdd = false },
            title = { Text("Rename List") },
            text = { Text("Are you sure you want to change the name?") },
            confirmButton = {
                TextButton(
                    onClick = {
                        rename(currentName)
                        showAdd = false
                    }
                ) { Text("Confirm") }
            },
            dismissButton = {
                TextButton(onClick = { showAdd = false }) { Text("Cancel") }
            }
        )
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = MaterialTheme.colorScheme.surface,
    ) {
        Column(
            verticalArrangement = Arrangement.spacedBy(4.dp),
            modifier = Modifier
                .padding(16.dp)
                .verticalScroll(rememberScrollState())
        ) {
            OutlinedTextField(
                currentName,
                onValueChange = { currentName = it },
                shape = MaterialTheme.shapes.large,
                trailingIcon = {
                    IconButton(
                        onClick = { showAdd = true },
                        enabled = currentName != customItem.item.name
                    ) { Icon(Icons.Default.Check, null) }
                },
                modifier = Modifier.fillMaxWidth()
            )
            Surface(
                shape = MaterialTheme.shapes.large
            ) {
                ListItem(
                    headlineContent = {},
                    leadingContent = {
                        val imageModifier = Modifier
                            .size(ComposableUtils.IMAGE_WIDTH / 3, ComposableUtils.IMAGE_HEIGHT / 3)
                            .clip(MaterialTheme.shapes.medium)

                        val imageHashing = customItem.toImageHash()

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
                                isNsfw = customItem.list.any { it.nsfw },
                                name = customItem.item.name,
                                hash = imageHashing?.hash,
                                modifier = imageModifier.let {
                                    if (!showNsfw && customItem.list.any { it.nsfw }) {
                                        it.blur(blurStrength)
                                    } else {
                                        it
                                    }
                                },
                            )
                        }
                    },
                    supportingContent = {
                        Column {

                        }
                    },
                    colors = ListItemDefaults.colors(
                        containerColor = Color.Transparent,
                    )
                )
            }

            HorizontalDivider()

            Text("List Count: ${customItem.list.size}")

            HorizontalDivider()

            FlowRow(
                horizontalArrangement = Arrangement.SpaceEvenly,
                modifier = Modifier.fillMaxWidth()
            ) {
                ActionItem(
                    onClick = {
                        scope.launch { sheetState.hide() }
                            .invokeOnCompletion {
                                onDismiss()
                                onRemoveItemsAction()
                            }
                    },
                    colors = CardDefaults.cardColors(
                        containerColor = Color.Transparent,
                        contentColor = MaterialTheme.colorScheme.error,
                    )
                ) {
                    Icon(Icons.Default.RemoveCircle, null)
                    Text("Remove Items")
                }

                ActionItem(
                    onClick = {
                        scope.launch { sheetState.hide() }
                            .invokeOnCompletion {
                                onDismiss()
                                onDeleteListAction()
                            }
                    },
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.errorContainer,
                        contentColor = MaterialTheme.colorScheme.onErrorContainer,
                    ),
                ) {
                    Icon(Icons.Default.Delete, null)
                    Text("Delete")
                }
            }
        }
    }
}

@Composable
private fun ActionItem(
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
    colors: CardColors = CardDefaults.cardColors(),
    content: @Composable ColumnScope.() -> Unit,
) {
    Card(
        onClick = onClick,
        colors = colors,
        modifier = modifier
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(8.dp),
            content = content
        )
    }
}