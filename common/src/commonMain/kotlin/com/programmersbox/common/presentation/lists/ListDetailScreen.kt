package com.programmersbox.common.presentation.lists

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColor
import androidx.compose.animation.core.animateDp
import androidx.compose.animation.core.updateTransition
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
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
import androidx.compose.material.icons.automirrored.filled.ArrowRightAlt
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.RemoveCircle
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.AppBarWithSearch
import androidx.compose.material3.BottomAppBar
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardColors
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
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
import androidx.compose.material3.SheetState
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.material3.rememberSearchBarState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalWindowInfo
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import chaintech.videoplayer.ui.preview.VideoPreviewComposable
import com.programmersbox.common.BackButton
import com.programmersbox.common.ComposableUtils
import com.programmersbox.common.DataStore
import com.programmersbox.common.NetworkConnectionRepository
import com.programmersbox.common.adaptiveGridCell
import com.programmersbox.common.db.CustomList
import com.programmersbox.common.db.CustomListInfo
import com.programmersbox.common.db.FavoriteType
import com.programmersbox.common.db.ListDao
import com.programmersbox.common.db.toImageHash
import com.programmersbox.common.presentation.components.HideScreen
import com.programmersbox.common.presentation.components.ImageSheet
import com.programmersbox.common.presentation.components.LoadingImage
import com.programmersbox.common.presentation.components.ModelOptionsSheet
import com.programmersbox.common.presentation.favorites.CoverCard
import dev.chrisbanes.haze.HazeProgressive
import dev.chrisbanes.haze.LocalHazeStyle
import dev.chrisbanes.haze.hazeEffect
import dev.chrisbanes.haze.hazeSource
import dev.chrisbanes.haze.rememberHazeState
import kotlinx.coroutines.launch
import org.koin.compose.koinInject
import org.koin.compose.viewmodel.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ListDetailScreen(
    onNavigateToDetail: (String) -> Unit,
    onNavigateToUser: (String) -> Unit,
    viewModel: ListDetailViewModel = koinViewModel(),
) {
    val connectionRepository = koinInject<NetworkConnectionRepository>()
    val shouldShowMedia by remember { derivedStateOf { connectionRepository.shouldShowMedia } }
    val dataStore = koinInject<DataStore>()
    val showBlur by dataStore.rememberShowBlur()
    val showNsfw by dataStore.showNsfw()
    val blurStrength by dataStore.hideNsfwStrength()

    val useProgressive by dataStore.rememberUseProgressive()
    val hazeState = rememberHazeState(showBlur)
    val hazeStyle = LocalHazeStyle.current

    val list = viewModel.customList

    list?.let { HideScreen(it.item.useBiometric) }

    var showInfo by remember { mutableStateOf(false) }
    val sheetState = rememberModalBottomSheetState(true)

    var showRemoveItems by remember { mutableStateOf(false) }

    if (showRemoveItems) {
        list?.let {
            ModalBottomSheet(
                onDismissRequest = { showRemoveItems = false },
                sheetState = rememberModalBottomSheetState(true),
                containerColor = MaterialTheme.colorScheme.surface,
            ) {
                RemoveItemsSheet(
                    customList = it,
                    showNsfw = showNsfw,
                    blurStrength = blurStrength.dp,
                    onDismiss = { showRemoveItems = false },
                    shouldShowMedia = shouldShowMedia,
                )
            }
        }
    }

    var showDeleteDialog by remember { mutableStateOf(false) }

    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("Delete List") },
            text = { Text("Are you sure you want to delete this list?") },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.deleteAll()
                        showDeleteDialog = false
                    }
                ) { Text("Yes") }
            },
            dismissButton = {
                TextButton(
                    onClick = { showDeleteDialog = false }
                ) { Text("No") }
            }
        )
    }

    if (showInfo) {
        list?.let { list ->
            InfoSheet(
                customItem = list,
                sheetState = sheetState,
                showNsfw = showNsfw,
                blurStrength = blurStrength.dp,
                rename = viewModel::rename,
                setBiometric = viewModel::setBiometric,
                onDismiss = { showInfo = false },
                onDeleteListAction = { showDeleteDialog = true },
                onRemoveItemsAction = { showRemoveItems = true },
                setNewCoverImage = { url, hash ->
                    viewModel.setCoverImage(url, hash)
                },
                shouldShowMedia = shouldShowMedia,
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

            AppBarWithSearch(
                state = searchBarState,
                inputField = {
                    SearchBarDefaults.InputField(
                        searchBarState = searchBarState,
                        textFieldState = viewModel.search,
                        enabled = LocalWindowInfo.current.isWindowFocused,
                        onSearch = {},
                        placeholder = {
                            Text(
                                modifier = Modifier.fillMaxWidth(),
                                text = "Search",
                                textAlign = TextAlign.Center,
                            )
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
                },
                navigationIcon = { BackButton() },
                actions = {
                    Text("(${list?.list?.size ?: 0})")
                    IconButton(
                        onClick = { showInfo = true }
                    ) { Icon(Icons.Default.Info, null) }
                },
                colors = appBarWithSearchColors,
                modifier = Modifier.hazeEffect(hazeState) {
                    progressive = if (useProgressive)
                        HazeProgressive.verticalGradient(
                            startIntensity = 1f,
                            endIntensity = 0f,
                            preferPerformance = true
                        )
                    else
                        null
                    style = hazeStyle
                }
            )
        }
    ) { padding ->
        LazyVerticalGrid(
            contentPadding = padding,
            verticalArrangement = Arrangement.spacedBy(4.dp),
            horizontalArrangement = Arrangement.spacedBy(4.dp),
            columns = adaptiveGridCell(minCount = 3),
            modifier = Modifier
                .fillMaxSize()
                .hazeSource(state = hazeState)
        ) {
            items(viewModel.searchedList) { item ->

                var sheetDetails by remember { mutableStateOf(false) }
                when (item.favoriteType) {
                    FavoriteType.Model -> {
                        if (sheetDetails) {
                            ModelOptionsSheet(
                                id = item.modelId,
                                imageUrl = item.imageUrl.orEmpty(),
                                hash = item.hash,
                                type = item.type,
                                name = item.name,
                                nsfw = item.nsfw,
                                creatorName = item.creatorName,
                                creatorImage = item.creatorImage,
                                description = item.description,
                                showSheet = sheetDetails,
                                onDialogDismiss = { sheetDetails = false },
                                onNavigateToDetail = onNavigateToDetail,
                                onNavigateToUser = onNavigateToUser,
                            )
                        }
                    }

                    FavoriteType.Image -> {
                        if (sheetDetails) {
                            ImageSheet(
                                url = item.imageUrl.orEmpty(),
                                isNsfw = item.nsfw,
                                isFavorite = false,
                                onFavorite = {},
                                onRemoveFromFavorite = {},
                                onDismiss = { sheetDetails = false },
                                nsfwText = "NSFW",
                                actions = {
                                    TextButton(
                                        onClick = {
                                            sheetDetails = false
                                            onNavigateToDetail(item.modelId.toString())
                                        }
                                    ) {
                                        Text("View Model")
                                        Icon(Icons.AutoMirrored.Filled.ArrowRightAlt, null)
                                    }
                                },
                                moreInfo = {}
                            )
                        }
                    }

                    FavoriteType.Creator -> {}
                }

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
                            FavoriteType.Image -> sheetDetails = true
                            FavoriteType.Creator -> onNavigateToUser(item.name)
                        }
                    },
                    shouldShowMedia = shouldShowMedia,
                    onLongClick = { sheetDetails = true },
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
    setBiometric: (Boolean) -> Unit,
    onDismiss: () -> Unit,
    onDeleteListAction: () -> Unit,
    onRemoveItemsAction: () -> Unit,
    setNewCoverImage: (String?, String?) -> Unit,
    shouldShowMedia: Boolean,
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

    var showCoverChange by remember { mutableStateOf(false) }

    if (showCoverChange) {
        val coverState = rememberModalBottomSheetState(true)
        ModalBottomSheet(
            onDismissRequest = { showCoverChange = false },
            sheetState = coverState,
            containerColor = MaterialTheme.colorScheme.surface,
        ) {
            LazyVerticalGrid(
                columns = adaptiveGridCell(minCount = 3),
                verticalArrangement = Arrangement.spacedBy(4.dp),
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                items(customItem.list) { item ->
                    Surface(
                        onClick = {
                            setNewCoverImage(item.imageUrl, item.hash)
                            scope.launch { coverState.hide() }
                                .invokeOnCompletion { showCoverChange = false }
                        }
                    ) {
                        ImageLoad(
                            url = item.imageUrl,
                            hash = item.hash,
                            nsfw = item.nsfw,
                            name = item.name,
                            showNsfw = showNsfw,
                            blurStrength = blurStrength,
                            shouldShowMedia = shouldShowMedia,
                            modifier = Modifier
                                .size(
                                    width = ComposableUtils.IMAGE_WIDTH,
                                    height = ComposableUtils.IMAGE_HEIGHT
                                )
                                .clip(MaterialTheme.shapes.medium)
                        )
                    }
                }
            }
        }
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = MaterialTheme.colorScheme.surface,
    ) {
        Column(
            verticalArrangement = Arrangement.spacedBy(4.dp),
            modifier = Modifier
                .padding(vertical = 16.dp)
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
                modifier = Modifier
                    .padding(horizontal = 16.dp)
                    .fillMaxWidth()
            )
            Surface(
                shape = MaterialTheme.shapes.large
            ) {
                ListItem(
                    overlineContent = {
                        val dataTimeFormatter = remember { createDateTimeFormatItem(true) }
                        Text("Last time updated: ${dataTimeFormatter.format(customItem.item.time.toLocalDateTime())}")
                    },
                    headlineContent = {},
                    leadingContent = {
                        Surface(
                            onClick = { showCoverChange = true }
                        ) {
                            Box(
                                contentAlignment = Alignment.Center,
                            ) {
                                val imageModifier = Modifier
                                    .size(
                                        ComposableUtils.IMAGE_WIDTH / 3,
                                        ComposableUtils.IMAGE_HEIGHT / 3
                                    )
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
                                        modifier = imageModifier.then(
                                            if (!showNsfw && customItem.list.any { it.nsfw }) {
                                                Modifier.blur(blurStrength)
                                            } else {
                                                Modifier
                                            }
                                        ),
                                    )
                                }
                                Icon(
                                    Icons.Default.Add,
                                    null
                                )
                            }
                        }
                    },
                    supportingContent = {
                        Column {
                            Text("Items: ${customItem.list.size}")
                            Text("Models: ${customItem.list.filter { it.favoriteType == FavoriteType.Model }.size}")
                            Text("Images: ${customItem.list.filter { it.favoriteType == FavoriteType.Image }.size}")
                            Text("Creators: ${customItem.list.filter { it.favoriteType == FavoriteType.Creator }.size}")
                        }
                    },
                    colors = ListItemDefaults.colors(
                        containerColor = Color.Transparent,
                    )
                )
            }

            HorizontalDivider()

            Card(
                onClick = { setBiometric(!customItem.item.useBiometric) },
                shape = RectangleShape
            ) {
                ListItem(
                    headlineContent = { Text("Use Biometrics to View?") },
                    trailingContent = {
                        Switch(
                            checked = customItem.item.useBiometric,
                            onCheckedChange = setBiometric
                        )
                    }
                )
            }

            HorizontalDivider()

            Text(
                "List Count: ${customItem.list.size}",
                modifier = Modifier.padding(horizontal = 16.dp)
            )

            HorizontalDivider()

            FlowRow(
                horizontalArrangement = Arrangement.SpaceEvenly,
                modifier = Modifier.fillMaxWidth()
            ) {
                ActionItem(
                    onClick = {
                        scope.launch { sheetState.hide() }
                            .invokeOnCompletion {
                                onRemoveItemsAction()
                                onDismiss()
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
                                onDeleteListAction()
                                onDismiss()
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
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
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

@Composable
fun ImageLoad(
    url: String?,
    hash: String?,
    nsfw: Boolean,
    name: String,
    showNsfw: Boolean,
    blurStrength: Dp,
    shouldShowMedia: Boolean,
    modifier: Modifier = Modifier,
) {
    if (url?.endsWith("mp4") == true && shouldShowMedia) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .background(Color.Black)
                .then(modifier)
        ) {
            VideoPreviewComposable(
                url = url,
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
            imageUrl = url.orEmpty(),
            isNsfw = nsfw,
            name = name,
            hash = hash,
            modifier = modifier.then(
                if (!showNsfw && nsfw) {
                    Modifier.blur(blurStrength)
                } else {
                    Modifier
                }
            )
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun RemoveItemsSheet(
    customList: CustomList,
    showNsfw: Boolean,
    blurStrength: Dp,
    shouldShowMedia: Boolean,
    onDismiss: () -> Unit,
    listRepository: ListDao = koinInject(),
) {
    val itemsToDelete = remember { mutableStateListOf<CustomListInfo>() }
    var showPopup by remember { mutableStateOf(false) }
    var removing by remember { mutableStateOf(false) }

    val scope = rememberCoroutineScope()

    if (showPopup) {
        val onPopupDismiss = { showPopup = false }

        AlertDialog(
            onDismissRequest = if (removing) {
                {}
            } else onPopupDismiss,
            title = { Text("Delete") },
            text = { Text("Are you sure you want to remove ${itemsToDelete.size} items from ${customList.item.name}?") },
            confirmButton = {
                TextButton(
                    onClick = {
                        removing = true
                        scope.launch {
                            runCatching {
                                itemsToDelete.forEach { item -> listRepository.removeItem(item) }
                                listRepository.updateFullList(customList.item)
                            }.onSuccess {
                                removing = false
                                itemsToDelete.clear()
                                onPopupDismiss()
                                onDismiss()
                            }
                        }
                    },
                    enabled = !removing
                ) { Text("Yes") }
            },
            dismissButton = { TextButton(onClick = onDismiss) { Text("No") } },
        )
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Delete Multiple?") },
                windowInsets = WindowInsets(0.dp),
            )
        },
        bottomBar = {
            BottomAppBar(
                contentPadding = PaddingValues(0.dp),
                windowInsets = WindowInsets(0.dp)
            ) {
                Button(
                    onClick = onDismiss,
                    modifier = Modifier
                        .weight(1f)
                        .padding(horizontal = 4.dp)
                ) { Text("Cancel") }

                Button(
                    onClick = { showPopup = true },
                    enabled = itemsToDelete.isNotEmpty(),
                    modifier = Modifier
                        .weight(1f)
                        .padding(horizontal = 4.dp)
                ) { Text("Remove") }
            }
        }
    ) { padding ->
        LazyVerticalGrid(
            columns = adaptiveGridCell(minCount = 3),
            verticalArrangement = Arrangement.spacedBy(4.dp),
            horizontalArrangement = Arrangement.spacedBy(4.dp),
            contentPadding = padding,
            modifier = Modifier.padding(4.dp),
        ) {
            items(customList.list) { item ->
                val transition = updateTransition(targetState = item in itemsToDelete, label = "")
                val outlineColor = MaterialTheme.colorScheme.outline
                Surface(
                    onClick = {
                        if (item in itemsToDelete)
                            itemsToDelete.remove(item)
                        else
                            itemsToDelete.add(item)
                    },
                    modifier = Modifier
                        .animateItem()
                        .border(
                            border = BorderStroke(
                                transition.animateDp(label = "border_width") { target -> if (target) 4.dp else 1.dp }.value,
                                transition.animateColor(label = "border_color") { target ->
                                    if (target) Color(
                                        0xfff44336
                                    ) else outlineColor
                                }.value
                            ),
                            shape = MaterialTheme.shapes.medium
                        )
                ) {
                    ImageLoad(
                        url = item.imageUrl,
                        hash = item.hash,
                        nsfw = item.nsfw,
                        name = item.name,
                        showNsfw = showNsfw,
                        blurStrength = blurStrength,
                        shouldShowMedia = shouldShowMedia,
                        modifier = Modifier
                            .size(
                                width = ComposableUtils.IMAGE_WIDTH,
                                height = ComposableUtils.IMAGE_HEIGHT
                            )
                            .clip(MaterialTheme.shapes.medium)
                    )
                }
            }
        }
    }
}