package com.programmersbox.common.presentation.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.PlaylistAdd
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Preview
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.ElevatedAssistChip
import androidx.compose.material3.ElevatedSuggestionChip
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.SegmentedListItem
import androidx.compose.material3.SuggestionChipDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveableStateHolder
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.programmersbox.common.ModelType
import com.programmersbox.common.db.FavoriteType
import com.programmersbox.common.db.FavoritesDao
import com.programmersbox.common.db.ListRepository
import com.programmersbox.common.presentation.qrcode.QrCodeType
import com.programmersbox.common.presentation.qrcode.ShareViaQrCode
import com.programmersbox.resources.Res
import com.programmersbox.resources.add_to_list
import com.programmersbox.resources.creators
import com.programmersbox.resources.favorite_model
import com.programmersbox.resources.nsfw
import com.programmersbox.resources.open
import com.programmersbox.resources.share
import com.programmersbox.resources.unfavorite_model
import com.programmersbox.resources.view_creators_models
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.koinInject

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun ModelOptionsSheet(
    id: Long,
    imageUrl: String?,
    hash: String?,
    name: String?,
    type: String?,
    description: String?,
    nsfw: Boolean,
    creatorName: String?,
    creatorImage: String?,
    showSheet: Boolean,
    onDialogDismiss: () -> Unit,
    onNavigateToDetail: (String) -> Unit,
    onNavigateToUser: ((String) -> Unit)? = null,
) {
    if (showSheet) {
        val dao = koinInject<FavoritesDao>()
        val isFavorite by dao
            .getFavoritesByType(FavoriteType.Model, id)
            .collectAsStateWithLifecycle(false)
        val listRepository = koinInject<ListRepository>()
        val scope = rememberCoroutineScope()

        val sheetState = rememberModalBottomSheetState(
            skipPartiallyExpanded = true
        )

        val colors =
            ListItemDefaults.colors(containerColor = MaterialTheme.colorScheme.surfaceContainer)

        val modelOptionsScope = rememberModelOptionsScope {
            group {
                add {
                    SegmentedListItem(
                        leadingContent = { Icon(Icons.Default.Preview, null) },
                        content = { Text(stringResource(Res.string.open)) },
                        onClick = {
                            onNavigateToDetail(id.toString())
                            scope.launch { sheetState.hide() }
                                .invokeOnCompletion { onDialogDismiss() }
                        },
                        shapes = it,
                        colors = colors
                    )
                }

                onNavigateToUser?.let { onNav ->
                    add {
                        SegmentedListItem(
                            leadingContent = {
                                creatorImage?.let { image ->
                                    LoadingImage(
                                        image,
                                        contentScale = ContentScale.FillBounds,
                                        modifier = Modifier
                                            .size(24.dp)
                                            .clip(CircleShape)
                                    )
                                }
                                    ?: Icon(Icons.Default.Person, null)
                            },
                            content = {
                                Text(
                                    stringResource(
                                        Res.string.view_creators_models,
                                        creatorName ?: stringResource(Res.string.creators)
                                    )
                                )
                            },
                            onClick = {
                                creatorName?.let { p1 -> onNav(p1) }
                                scope.launch { sheetState.hide() }
                                    .invokeOnCompletion { onDialogDismiss() }
                            },
                            shapes = it,
                            colors = colors
                        )
                    }
                }
            }

            item {
                var showQrCode by remember { mutableStateOf(false) }

                if (showQrCode) {
                    ShareViaQrCode(
                        title = name.orEmpty(),
                        url = "https://civitai.com/models/$id",
                        qrCodeType = QrCodeType.Model,
                        id = id.toString(),
                        username = "",
                        imageUrl = imageUrl.orEmpty(),
                        onClose = { showQrCode = false }
                    )
                }

                ListItem(
                    leadingContent = { Icon(Icons.Default.Share, null) },
                    content = { Text(stringResource(Res.string.share)) },
                    colors = colors,
                    shapes = it,
                    onClick = { showQrCode = true }
                )
            }

            group {
                add {
                    if (isFavorite) {
                        SegmentedListItem(
                            leadingContent = { Icon(Icons.Default.Favorite, null) },
                            content = { Text(stringResource(Res.string.unfavorite_model)) },
                            colors = colors,
                            shapes = it,
                            onClick = {
                                scope.launch {
                                    dao.removeModel(id)
                                    sheetState.hide()
                                }.invokeOnCompletion { onDialogDismiss() }
                            },
                        )
                    } else {
                        SegmentedListItem(
                            leadingContent = { Icon(Icons.Default.Favorite, null) },
                            content = { Text(stringResource(Res.string.favorite_model)) },
                            onClick = {
                                scope.launch {
                                    dao.addFavorite(
                                        id = id,
                                        name = name.orEmpty(),
                                        description = description,
                                        type = runCatching { ModelType.valueOf(type!!) }
                                            .getOrElse { ModelType.Other },
                                        nsfw = nsfw,
                                        imageUrl = imageUrl,
                                        favoriteType = FavoriteType.Model,
                                        modelId = id,
                                        hash = hash,
                                        creatorName = creatorName,
                                        creatorImage = creatorImage
                                    )
                                    sheetState.hide()
                                }.invokeOnCompletion { onDialogDismiss() }
                            },
                            shapes = it,
                            colors = colors
                        )
                    }
                }

                add {
                    var showLists by remember { mutableStateOf(false) }
                    val listState = rememberModalBottomSheetState(true)

                    if (showLists) {
                        ModalBottomSheet(
                            onDismissRequest = { showLists = false },
                            containerColor = MaterialTheme.colorScheme.surface,
                            sheetState = listState
                        ) {
                            ListChoiceScreen(
                                id = id,
                                onAdd = { selectedLists ->
                                    scope.launch {
                                        listRepository.addToMultipleLists(
                                            selectedLists = selectedLists,
                                            id = id,
                                            name = name.orEmpty(),
                                            description = description,
                                            type = type.orEmpty(),
                                            nsfw = nsfw,
                                            imageUrl = imageUrl,
                                            favoriteType = FavoriteType.Model,
                                            hash = hash,
                                            creatorName = creatorName,
                                            creatorImage = creatorImage,
                                        )
                                        listState.hide()
                                    }.invokeOnCompletion { showLists = false }
                                },
                                navigationIcon = {
                                    IconButton(
                                        onClick = { showLists = false }
                                    ) { Icon(Icons.Default.Close, null) }
                                },
                            )
                        }
                    }

                    SegmentedListItem(
                        leadingContent = { Icon(Icons.AutoMirrored.Filled.PlaylistAdd, null) },
                        content = { Text(stringResource(Res.string.add_to_list)) },
                        colors = colors,
                        shapes = it,
                        onClick = { showLists = true }
                    )
                }
            }
        }

        ModalBottomSheet(
            onDismissRequest = onDialogDismiss,
            sheetState = sheetState,
            containerColor = MaterialTheme.colorScheme.surface,
        ) {
            Column(
                modifier = Modifier.verticalScroll(rememberScrollState())
            ) {
                TopAppBar(
                    title = { Text(name.orEmpty()) },
                )

                FlowRow(
                    itemVerticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    modifier = Modifier.padding(8.dp)
                ) {
                    ElevatedSuggestionChip(
                        label = { Text(type.orEmpty()) },
                        onClick = {},
                        colors = SuggestionChipDefaults.elevatedSuggestionChipColors(
                            disabledLabelColor = MaterialTheme.colorScheme.onSurface,
                            disabledContainerColor = MaterialTheme.colorScheme.surfaceContainerLow
                        ),
                        border = BorderStroke(
                            1.dp,
                            MaterialTheme.colorScheme.onSurface
                        ),
                        enabled = false,
                    )

                    if (nsfw) {
                        ElevatedAssistChip(
                            label = { Text(stringResource(Res.string.nsfw)) },
                            onClick = {},
                            colors = AssistChipDefaults.elevatedAssistChipColors(
                                disabledLabelColor = MaterialTheme.colorScheme.error,
                                disabledContainerColor = MaterialTheme.colorScheme.surface
                            ),
                            enabled = false,
                            border = BorderStroke(
                                1.dp,
                                MaterialTheme.colorScheme.error,
                            ),
                        )
                    }
                }

                val stateHolder = rememberSaveableStateHolder()

                Column(
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.padding(8.dp)
                ) {
                    for (index in 0 until modelOptionsScope.size) {
                        stateHolder.SaveableStateProvider(index) {
                            when (val item = modelOptionsScope[index]) {
                                is ModelOptionsType.Group -> {
                                    Column(
                                        verticalArrangement = Arrangement.spacedBy(ListItemDefaults.SegmentedGap),
                                    ) {
                                        item.content.forEachIndexed { itemIndex, function ->
                                            function(
                                                ListItemDefaults.segmentedShapes(
                                                    index = itemIndex,
                                                    count = item.content.size
                                                )
                                            )
                                        }
                                    }
                                }

                                is ModelOptionsType.Item -> item.content(
                                    ListItemDefaults.shapes(
                                        shape = MaterialTheme.shapes.large
                                    )
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
