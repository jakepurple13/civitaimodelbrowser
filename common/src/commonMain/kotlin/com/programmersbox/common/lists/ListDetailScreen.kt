package com.programmersbox.common.lists

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.input.clearText
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.RemoveCircle
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.AppBarWithSearch
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
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SearchBarDefaults
import androidx.compose.material3.SearchBarValue
import androidx.compose.material3.SheetState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.material3.rememberSearchBarState
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
import androidx.compose.ui.platform.LocalWindowInfo
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import chaintech.videoplayer.ui.preview.VideoPreviewComposable
import com.programmersbox.common.BackButton
import com.programmersbox.common.ComposableUtils
import com.programmersbox.common.DataStore
import com.programmersbox.common.adaptiveGridCell
import com.programmersbox.common.components.LoadingImage
import com.programmersbox.common.db.CoverCard
import com.programmersbox.common.db.CustomList
import com.programmersbox.common.db.FavoriteType
import com.programmersbox.common.db.toImageHash
import com.programmersbox.common.ifTrue
import dev.chrisbanes.haze.HazeProgressive
import dev.chrisbanes.haze.HazeState
import dev.chrisbanes.haze.LocalHazeStyle
import dev.chrisbanes.haze.hazeEffect
import dev.chrisbanes.haze.hazeSource
import kotlinx.coroutines.launch
import org.koin.compose.koinInject
import org.koin.compose.viewmodel.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ListDetailScreen(
    viewModel: ListDetailViewModel = koinViewModel(),
    onNavigateToDetail: (String) -> Unit,
    onNavigateToUser: (String) -> Unit,
) {
    val dataStore = koinInject<DataStore>()
    val showBlur by dataStore.rememberShowBlur()
    val showNsfw by remember { dataStore.showNsfw.flow }
        .collectAsStateWithLifecycle(false)
    val blurStrength by remember { dataStore.hideNsfwStrength.flow }
        .collectAsStateWithLifecycle(6f)

    val hazeState = remember { HazeState() }
    val hazeStyle = LocalHazeStyle.current

    val list = viewModel.customList

    var showInfo by remember { mutableStateOf(false) }
    val sheetState = rememberModalBottomSheetState(true)

    if (showInfo) {
        list?.let { list ->
            InfoSheet(
                customItem = list,
                sheetState = sheetState,
                showNsfw = showNsfw,
                blurStrength = blurStrength.dp,
                rename = viewModel::rename,
                onDismiss = { showInfo = false },
                onDeleteListAction = viewModel::deleteAll,
                //TODO: Need to put in
                onRemoveItemsAction = {}//viewModel::removeItems,
            )
        }
    }

    val searchBarState = rememberSearchBarState()

    Scaffold(
        topBar = {
            val appBarWithSearchColors = SearchBarDefaults.appBarWithSearchColors(
                searchBarColors = SearchBarDefaults.colors(
                    containerColor = if (showBlur)
                        Color.Transparent
                    else
                        MaterialTheme.colorScheme.surfaceContainerHigh
                ),
                appBarContainerColor = if (showBlur)
                    Color.Transparent
                else
                    MaterialTheme.colorScheme.surface
            )

            val inputField = @Composable {
                SearchBarDefaults.InputField(
                    searchBarState = searchBarState,
                    textFieldState = viewModel.search,
                    enabled = LocalWindowInfo.current.isWindowFocused,
                    onSearch = {},
                    placeholder = {
                        if (searchBarState.currentValue == SearchBarValue.Collapsed) {
                            Text(
                                modifier = Modifier.fillMaxWidth(),
                                text = "Search",
                                textAlign = TextAlign.Center,
                            )
                        }
                    },
                    trailingIcon = {
                        AnimatedVisibility(
                            viewModel.search.text.isNotEmpty(),
                            enter = slideInHorizontally { it } + fadeIn(),
                            exit = slideOutHorizontally { it } + fadeOut()
                        ) {
                            IconButton(
                                onClick = { viewModel.search.clearText() }
                            ) { Icon(Icons.Default.Clear, null) }
                        }
                    },
                )
            }

            AppBarWithSearch(
                state = searchBarState,
                inputField = inputField,
                navigationIcon = { BackButton() },
                actions = {
                    Text("(${list?.list?.size ?: 0})")
                    IconButton(
                        onClick = { showInfo = true }
                    ) { Icon(Icons.Default.Info, null) }
                },
                colors = appBarWithSearchColors,
                modifier = Modifier.ifTrue(showBlur) {
                    hazeEffect(hazeState) {
                        progressive = HazeProgressive.verticalGradient(
                            startIntensity = 1f,
                            endIntensity = 0f,
                            preferPerformance = true
                        )
                        style = hazeStyle
                    }
                }
            )
        }
    ) { padding ->
        LazyVerticalGrid(
            contentPadding = padding,
            verticalArrangement = Arrangement.spacedBy(4.dp),
            horizontalArrangement = Arrangement.spacedBy(4.dp),
            columns = adaptiveGridCell(),
            modifier = Modifier
                .fillMaxSize()
                .ifTrue(showBlur) { hazeSource(state = hazeState) }
        ) {
            items(viewModel.searchedList) { item ->
                CoverCard(
                    imageUrl = item.imageUrl.orEmpty(),
                    name = item.name,
                    type = item.type,
                    isNsfw = item.nsfw,
                    showNsfw = showNsfw,
                    blurStrength = blurStrength.dp,
                    blurHash = item.hash,
                    onClick = {
                        when (item.favoriteType) {
                            FavoriteType.Model -> onNavigateToDetail(item.id.toString())
                            FavoriteType.Image -> TODO()
                            FavoriteType.Creator -> onNavigateToUser(item.name)
                        }
                    },
                    onLongClick = {

                    },
                    modifier = Modifier
                        .size(
                            width = ComposableUtils.IMAGE_WIDTH,
                            height = ComposableUtils.IMAGE_HEIGHT
                        )
                        .clip(MaterialTheme.shapes.medium)
                        .animateItem()
                )
            }
        }
    }
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