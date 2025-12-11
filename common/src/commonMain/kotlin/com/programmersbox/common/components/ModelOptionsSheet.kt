package com.programmersbox.common.components

import androidx.compose.foundation.lazy.layout.MutableIntervalList
import androidx.compose.foundation.shape.CornerSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.PlaylistAdd
import androidx.compose.material.icons.filled.Block
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Preview
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
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
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.dp
import com.programmersbox.common.Models
import com.programmersbox.common.db.BlacklistedItemRoom
import com.programmersbox.common.db.FavoriteModel
import com.programmersbox.common.db.FavoriteType
import com.programmersbox.common.db.FavoritesDao
import com.programmersbox.common.home.BlacklistHandling
import com.programmersbox.common.qrcode.QrCodeType
import com.programmersbox.common.qrcode.ShareViaQrCode
import kotlinx.coroutines.launch
import org.koin.compose.koinInject

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ModelOptionsSheet(
    models: Models,
    database: List<FavoriteModel>,
    blacklisted: List<BlacklistedItemRoom>,
    isBlacklisted: Boolean,
    showSheet: Boolean,
    onDialogDismiss: () -> Unit,
    onNavigateToDetail: (String) -> Unit,
    onNavigateToUser: ((String) -> Unit)? = null,
    onNavigateToDetailImages: ((Long, String) -> Unit)? = null,
) {
    if (showSheet) {
        val dao = koinInject<FavoritesDao>()
        val scope = rememberCoroutineScope()
        var showDialog by remember { mutableStateOf(false) }

        val sheetState = rememberModalBottomSheetState(
            skipPartiallyExpanded = true
        )

        BlacklistHandling(
            blacklisted = blacklisted,
            modelId = models.id,
            name = models.name,
            nsfw = models.nsfw,
            showDialog = showDialog,
            onDialogDismiss = {
                showDialog = false
                scope.launch { sheetState.hide() }
                    .invokeOnCompletion { onDialogDismiss() }
            }
        )

        val modelImage = remember(models) {
            models
                .modelVersions
                .firstOrNull()
                ?.images
                ?.firstOrNull { it.url.isNotEmpty() }
        }

        val modelOptionsScope = rememberModelOptionsScope {
            item {
                if (isBlacklisted) {
                    Card(
                        onClick = { showDialog = true },
                        shape = it
                    ) {
                        ListItem(
                            leadingContent = { Icon(Icons.Default.Block, null) },
                            headlineContent = { Text("Unblacklist") }
                        )
                    }
                } else {
                    Card(
                        onClick = { showDialog = true },
                        shape = it
                    ) {
                        ListItem(
                            leadingContent = { Icon(Icons.Default.Block, null) },
                            headlineContent = { Text("Blacklist") }
                        )
                    }
                }
            }

            item {
                Card(
                    onClick = {
                        onNavigateToDetail(models.id.toString())
                        scope.launch { sheetState.hide() }
                            .invokeOnCompletion { onDialogDismiss() }
                    },
                    shape = it
                ) {
                    ListItem(
                        leadingContent = { Icon(Icons.Default.Preview, null) },
                        headlineContent = { Text("Open") }
                    )
                }
            }

            onNavigateToUser?.let { onNav ->
                item {
                    Card(
                        onClick = {
                            onNav(models.creator?.username.orEmpty())
                            scope.launch { sheetState.hide() }
                                .invokeOnCompletion { onDialogDismiss() }
                        },
                        shape = it
                    ) {
                        ListItem(
                            leadingContent = { Icon(Icons.Default.Person, null) },
                            headlineContent = { Text("Open Creator") }
                        )
                    }
                }
            }

            onNavigateToDetailImages?.let { onNav ->
                item {
                    Card(
                        onClick = {
                            onNav(models.id, models.name)
                            scope.launch { sheetState.hide() }
                                .invokeOnCompletion { onDialogDismiss() }
                        },
                        shape = it
                    ) {
                        ListItem(
                            leadingContent = { Icon(Icons.Default.Image, null) },
                            headlineContent = { Text("Open Images") }
                        )
                    }
                }
            }

            item {
                var showQrCode by remember { mutableStateOf(false) }

                if (showQrCode) {
                    ShareViaQrCode(
                        title = models.name,
                        url = "https://civitai.com/models/${models.id}",
                        qrCodeType = QrCodeType.Model,
                        id = models.id.toString(),
                        username = models.creator?.username,
                        imageUrl = modelImage?.url.orEmpty(),
                        onClose = { showQrCode = false }
                    )
                }
                Card(
                    onClick = { showQrCode = true },
                    shape = it
                ) {
                    ListItem(
                        leadingContent = { Icon(Icons.Default.Share, null) },
                        headlineContent = { Text("Share") }
                    )
                }
            }

            item {
                if (database.any { m -> m.id == models.id }) {
                    Card(
                        onClick = {
                            scope.launch {
                                dao.removeModel(models.id)
                                sheetState.hide()
                            }.invokeOnCompletion { onDialogDismiss() }
                        },
                        shape = it
                    ) {
                        ListItem(
                            leadingContent = { Icon(Icons.Default.Favorite, null) },
                            headlineContent = { Text("Unfavorite Model") }
                        )
                    }
                } else {
                    Card(
                        onClick = {
                            scope.launch {
                                dao.addFavorite(
                                    id = models.id,
                                    name = models.name,
                                    description = models.description,
                                    type = models.type,
                                    nsfw = models.nsfw,
                                    imageUrl = models.modelVersions.firstOrNull()?.images?.firstOrNull()?.url,
                                    favoriteType = FavoriteType.Model,
                                    modelId = models.id
                                )
                                sheetState.hide()
                            }.invokeOnCompletion { onDialogDismiss() }
                        },
                        shape = it
                    ) {
                        ListItem(
                            leadingContent = { Icon(Icons.Default.Favorite, null) },
                            headlineContent = { Text("Favorite Model") }
                        )
                    }
                }
            }

            item {
                Card(
                    onClick = {
                        TODO("Add to list")
                    },
                    shape = it
                ) {
                    ListItem(
                        leadingContent = { Icon(Icons.AutoMirrored.Filled.PlaylistAdd, null) },
                        headlineContent = { Text("Add to List") }
                    )
                }
            }
        }

        ModalBottomSheet(
            onDismissRequest = onDialogDismiss,
            sheetState = sheetState,
            containerColor = MaterialTheme.colorScheme.surface,
        ) {
            TopAppBar(
                title = { Text(models.name) },
            )

            val stateHolder = rememberSaveableStateHolder()

            for (index in 0 until modelOptionsScope.size) {
                stateHolder.SaveableStateProvider(index) {
                    modelOptionsScope[index](
                        when (index) {
                            0 -> MaterialTheme.shapes.medium.copy(
                                bottomStart = CornerSize(0.dp),
                                bottomEnd = CornerSize(0.dp)
                            )

                            modelOptionsScope.size - 1 -> MaterialTheme.shapes.medium.copy(
                                topStart = CornerSize(0.dp),
                                topEnd = CornerSize(0.dp)
                            )

                            else -> RoundedCornerShape(0.dp)
                        }
                    )
                    if (index < modelOptionsScope.size - 1) {
                        HorizontalDivider()
                    }
                }
            }
        }
    }
}

interface ModelOptionsScope {
    fun item(content: @Composable (Shape) -> Unit)

    val size: Int

    operator fun get(index: Int): @Composable (Shape) -> Unit
}

private class ModelOptionsScopeImpl(
    content: ModelOptionsScope.() -> Unit = {},
) : ModelOptionsScope {
    val intervals: MutableIntervalList<@Composable (Shape) -> Unit> = MutableIntervalList()

    override val size: Int get() = intervals.size

    override fun get(index: Int): @Composable ((Shape) -> Unit) = intervals[index].value

    init {
        apply(content)
    }

    override fun item(content: @Composable (Shape) -> Unit) {
        intervals.addInterval(1, content)
    }
}

@Composable
fun rememberModelOptionsScope(
    content: ModelOptionsScope.() -> Unit = {},
): ModelOptionsScope = remember { ModelOptionsScopeImpl(content) }