package com.programmersbox.common.db

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CornerSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowRightAlt
import androidx.compose.material.icons.automirrored.filled.PlaylistAdd
import androidx.compose.material.icons.automirrored.filled.Sort
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Preview
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Card
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.DockedSearchBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledIconToggleButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SearchBarDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveableStateHolder
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import com.dokar.sonner.ToastType
import com.dokar.sonner.ToasterState
import com.programmersbox.common.BackButton
import com.programmersbox.common.ComposableUtils
import com.programmersbox.common.CustomScrollBar
import com.programmersbox.common.DataStore
import com.programmersbox.common.NetworkConnectionRepository
import com.programmersbox.common.SheetDetails
import com.programmersbox.common.adaptiveGridCell
import com.programmersbox.common.components.ImageSheet
import com.programmersbox.common.components.ListChoiceScreen
import com.programmersbox.common.components.rememberModelOptionsScope
import com.programmersbox.common.home.CardContent
import com.programmersbox.common.ifTrue
import com.programmersbox.common.isScrollingUp
import com.programmersbox.common.qrcode.QrCodeType
import com.programmersbox.common.qrcode.ShareViaQrCode
import dev.chrisbanes.haze.HazeProgressive
import dev.chrisbanes.haze.HazeState
import dev.chrisbanes.haze.LocalHazeStyle
import dev.chrisbanes.haze.hazeEffect
import dev.chrisbanes.haze.hazeSource
import kotlinx.coroutines.launch
import org.koin.compose.koinInject
import org.koin.compose.viewmodel.koinViewModel

internal const val IMAGE_FILTER = "Image"
internal const val CREATOR_FILTER = "Creator"

@OptIn(
    ExperimentalFoundationApi::class,
    ExperimentalMaterial3Api::class,
    ExperimentalLayoutApi::class
)
@Composable
fun FavoritesUI(
    viewModel: FavoritesViewModel = koinViewModel(),
    onNavigateToDetail: (Long) -> Unit,
    onNavigateToUser: (String) -> Unit,
) {
    val connectionRepository = koinInject<NetworkConnectionRepository>()
    val shouldShowMedia by remember { derivedStateOf { connectionRepository.shouldShowMedia } }
    val hazeState = remember { HazeState() }
    val dataStore = koinInject<DataStore>()
    val scope = rememberCoroutineScope()
    val showNsfw by dataStore.showNsfw()
    val blurStrength by dataStore.hideNsfwStrength()
    var reverseFavorites by dataStore.rememberReverseFavorites()
    val showBlur by dataStore.rememberShowBlur()
    val lazyGridState = rememberLazyGridState()

    var showSortedByDialog by remember { mutableStateOf(false) }

    val hazeStyle = LocalHazeStyle.current

    if (showSortedByDialog) {
        SheetDetails(
            onDismiss = { showSortedByDialog = false },
            content = {
                Surface {
                    Column(
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        CenterAlignedTopAppBar(
                            title = { Text("Sort By") },
                            windowInsets = WindowInsets(0.dp),
                            colors = TopAppBarDefaults.topAppBarColors(
                                containerColor = Color.Transparent
                            )
                        )

                        FlowRow(
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(24.dp)
                        ) {
                            SortedBy.entries.forEach {
                                FilterChip(
                                    selected = it == viewModel.sortedBy,
                                    label = { Text(it.name) },
                                    onClick = { viewModel.sortedBy = it }
                                )
                            }
                        }
                    }
                }
            }
        )
    }

    Scaffold(
        topBar = {
            Surface(
                color = if (showBlur) Color.Transparent else MaterialTheme.colorScheme.surface
            ) {
                Column(
                    modifier = Modifier.ifTrue(showBlur) {
                        hazeEffect(hazeState, hazeStyle) {
                            progressive = HazeProgressive.verticalGradient(
                                startIntensity = 1f,
                                endIntensity = 0f,
                                preferPerformance = true
                            )
                        }
                    }
                ) {
                    DockedSearchBar(
                        expanded = false,
                        onExpandedChange = {},
                        inputField = {
                            SearchBarDefaults.InputField(
                                query = viewModel.search,
                                onQueryChange = { viewModel.search = it },
                                onSearch = {},
                                expanded = false,
                                onExpandedChange = {},
                                leadingIcon = { BackButton() },
                                placeholder = { Text("Search Favorites") },
                                trailingIcon = {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text("(${viewModel.favoritesList.size})")
                                        FilledIconToggleButton(
                                            checked = reverseFavorites,
                                            onCheckedChange = { reverseFavorites = it }
                                        ) { Icon(Icons.Default.Image, null) }
                                        IconButton(
                                            onClick = { showSortedByDialog = true }
                                        ) { Icon(Icons.AutoMirrored.Filled.Sort, null) }
                                        AnimatedVisibility(viewModel.search.isNotEmpty()) {
                                            IconButton(
                                                onClick = { viewModel.search = "" }
                                            ) { Icon(Icons.Default.Clear, null) }
                                        }
                                    }
                                },
                                modifier = Modifier.fillMaxWidth()
                            )
                        },
                        colors = if (showBlur) SearchBarDefaults.colors(
                            containerColor = Color.Transparent
                        ) else SearchBarDefaults.colors(),
                        modifier = Modifier
                            .fillMaxWidth()
                            .windowInsetsPadding(SearchBarDefaults.windowInsets)
                    ) {}
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 4.dp)
                    ) {
                        items(viewModel.typeList) {
                            FilterChip(
                                selected = it in viewModel.filterList,
                                onClick = {
                                    viewModel.toggleFilter(it)
                                    scope.launch { lazyGridState.animateScrollToItem(0) }
                                },
                                label = { Text(it) }
                            )
                        }

                        item {
                            FilterChip(
                                selected = IMAGE_FILTER in viewModel.filterList,
                                onClick = {
                                    viewModel.toggleFilter(IMAGE_FILTER)
                                    scope.launch { lazyGridState.animateScrollToItem(0) }
                                },
                                label = { Text(IMAGE_FILTER) }
                            )
                        }

                        item {
                            FilterChip(
                                selected = CREATOR_FILTER in viewModel.filterList,
                                onClick = {
                                    viewModel.toggleFilter(CREATOR_FILTER)
                                    scope.launch { lazyGridState.animateScrollToItem(0) }
                                },
                                label = { Text(CREATOR_FILTER) }
                            )
                        }
                    }
                }
            }
        },
        floatingActionButton = {
            AnimatedVisibility(
                visible = lazyGridState.isScrollingUp() && lazyGridState.firstVisibleItemIndex > 0,
                enter = fadeIn() + slideInHorizontally { it },
                exit = slideOutHorizontally { it } + fadeOut()
            ) {
                FloatingActionButton(
                    onClick = { scope.launch { lazyGridState.animateScrollToItem(0) } },
                ) { Icon(Icons.Default.ArrowUpward, null) }
            }
        },
    ) { padding ->
        LazyVerticalGrid(
            state = lazyGridState,
            columns = adaptiveGridCell(),
            verticalArrangement = Arrangement.spacedBy(4.dp),
            horizontalArrangement = Arrangement.spacedBy(4.dp),
            contentPadding = padding,
            modifier = Modifier
                .padding(4.dp)
                .ifTrue(showBlur) { hazeSource(state = hazeState) }
                .fillMaxSize()
        ) {
            items(
                viewModel.viewingList,
                key = {
                    when (it) {
                        is FavoriteModel.Creator -> it.name + "Creator"
                        is FavoriteModel.Image -> it.id.toString() + it.imageUrl + "Image"
                        is FavoriteModel.Model -> it.id.toString() + "Model"
                    }
                },
                contentType = {
                    when (it) {
                        is FavoriteModel.Creator -> "creator"
                        is FavoriteModel.Image -> "image"
                        is FavoriteModel.Model -> "model"
                    }
                }
            ) { model ->
                when (model) {
                    is FavoriteModel.Creator -> {
                        var showSheet by remember { mutableStateOf(false) }

                        FavoritesCreatorOptionsSheet(
                            models = model,
                            showSheet = showSheet,
                            onDialogDismiss = { showSheet = false },
                            onNavigateToUser = onNavigateToUser,
                        )

                        CreatorItem(
                            models = model,
                            onClick = { onNavigateToUser(model.name) },
                            onLongClick = { showSheet = true },
                            modifier = Modifier.animateItem()
                        )
                    }

                    is FavoriteModel.Image -> {
                        var sheetDetails by remember { mutableStateOf<FavoriteModel.Image?>(null) }

                        sheetDetails?.let { sheetModel ->
                            ImageSheet(
                                url = sheetModel.imageUrl.orEmpty(),
                                isNsfw = sheetModel.nsfw,
                                isFavorite = true,
                                onFavorite = {},
                                onRemoveFromFavorite = {
                                    viewModel.removeImage(sheetModel.imageUrl.orEmpty())
                                },
                                onDismiss = { sheetDetails = null },
                                nsfwText = "NSFW",
                                actions = {
                                    TextButton(
                                        onClick = {
                                            sheetDetails = null
                                            onNavigateToDetail(sheetModel.modelId)
                                        }
                                    ) {
                                        Text("View Model")
                                        Icon(Icons.AutoMirrored.Filled.ArrowRightAlt, null)
                                    }
                                },
                                moreInfo = {
                                    sheetModel.imageMetaDb?.let { meta ->
                                        Column(
                                            modifier = Modifier.padding(4.dp)
                                        ) {
                                            meta.model?.let { Text("Model: $it") }
                                            HorizontalDivider()
                                            meta.prompt?.let { Text("Prompt: $it") }
                                            HorizontalDivider()
                                            meta.negativePrompt?.let { Text("Negative Prompt: $it") }
                                            HorizontalDivider()
                                            meta.seed?.let { Text("Seed: $it") }
                                            HorizontalDivider()
                                            meta.sampler?.let { Text("Sampler: $it") }
                                            HorizontalDivider()
                                            meta.steps?.let { Text("Steps: $it") }
                                            HorizontalDivider()
                                            meta.clipSkip?.let { Text("Clip Skip: $it") }
                                            HorizontalDivider()
                                            meta.size?.let { Text("Size: $it") }
                                            HorizontalDivider()
                                            meta.cfgScale?.let { Text("Cfg Scale: $it") }
                                        }
                                    }
                                }
                            )
                        }
                        ImageItem(
                            models = model,
                            onClick = { sheetDetails = model },
                            showNsfw = showNsfw,
                            blurStrength = blurStrength.dp,
                            blurHash = model.hash,
                            shouldShowMedia = shouldShowMedia,
                            modifier = Modifier.animateItem()
                        )
                    }

                    is FavoriteModel.Model -> {
                        var showSheet by remember { mutableStateOf(false) }

                        FavoritesModelOptionsSheet(
                            models = model,
                            showSheet = showSheet,
                            onDialogDismiss = { showSheet = false },
                            onNavigateToDetail = onNavigateToDetail,
                        )

                        ModelItem(
                            models = model,
                            onClick = { onNavigateToDetail(model.id) },
                            showNsfw = showNsfw,
                            blurStrength = blurStrength.dp,
                            onLongClick = { showSheet = true },
                            blurHash = model.hash,
                            shouldShowMedia = shouldShowMedia,
                            modifier = Modifier.animateItem()
                        )
                    }
                }
            }
        }
        CustomScrollBar(
            lazyGridState = lazyGridState,
            modifier = Modifier
                .zIndex(5f)
                .padding(4.dp)
                .padding(padding)
        )
    }
}

@Composable
private fun CreatorItem(
    models: FavoriteModel.Creator,
    onClick: () -> Unit,
    onLongClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    CoverCard(
        imageUrl = models.imageUrl.orEmpty(),
        name = models.name,
        type = CREATOR_FILTER,
        isNsfw = false,
        showNsfw = true,
        blurStrength = 0.dp,
        onClick = onClick,
        onLongClick = onLongClick,
        blurHash = null,
        shouldShowMedia = false,
        modifier = modifier.size(
            width = ComposableUtils.IMAGE_WIDTH,
            height = ComposableUtils.IMAGE_HEIGHT
        )
    )
}

@Composable
private fun ImageItem(
    models: FavoriteModel.Image,
    showNsfw: Boolean,
    blurStrength: Dp,
    onClick: () -> Unit,
    shouldShowMedia: Boolean,
    modifier: Modifier = Modifier,
    blurHash: String? = null,
) {
    CoverCard(
        imageUrl = models.imageUrl.orEmpty(),
        name = models.name,
        type = IMAGE_FILTER,
        isNsfw = models.nsfw,
        showNsfw = showNsfw,
        blurStrength = blurStrength,
        onClick = onClick,
        blurHash = blurHash,
        shouldShowMedia = shouldShowMedia,
        modifier = modifier.size(
            width = ComposableUtils.IMAGE_WIDTH,
            height = ComposableUtils.IMAGE_HEIGHT
        )
    )
}

@Composable
private fun ModelItem(
    models: FavoriteModel.Model,
    showNsfw: Boolean,
    blurStrength: Dp,
    onClick: () -> Unit,
    onLongClick: () -> Unit,
    blurHash: String?,
    shouldShowMedia: Boolean,
    modifier: Modifier = Modifier,
) {
    CoverCard(
        imageUrl = models.imageUrl.orEmpty(),
        name = models.name,
        type = models.type,
        isNsfw = models.nsfw,
        showNsfw = showNsfw,
        blurStrength = blurStrength,
        onClick = onClick,
        onLongClick = onLongClick,
        blurHash = blurHash,
        shouldShowMedia = shouldShowMedia,
        modifier = modifier.size(
            width = ComposableUtils.IMAGE_WIDTH,
            height = ComposableUtils.IMAGE_HEIGHT
        )
    )
}

@Composable
fun CoverCard(
    imageUrl: String,
    name: String,
    type: String,
    isNsfw: Boolean,
    showNsfw: Boolean,
    blurStrength: Dp,
    blurHash: String?,
    shouldShowMedia: Boolean,
    modifier: Modifier = Modifier,
    onClick: () -> Unit = {},
) {
    Surface(
        onClick = onClick,
        tonalElevation = 4.dp,
        shape = MaterialTheme.shapes.medium,
        modifier = modifier
    ) {
        CardContent(
            imageUrl = imageUrl,
            name = name,
            type = type,
            isNsfw = isNsfw,
            showNsfw = showNsfw,
            blurHash = blurHash,
            blurStrength = blurStrength,
            shouldShowMedia = shouldShowMedia,
        )
    }
}

@Composable
fun CoverCard(
    imageUrl: String,
    name: String,
    type: String,
    isNsfw: Boolean,
    showNsfw: Boolean,
    blurStrength: Dp,
    blurHash: String?,
    shouldShowMedia: Boolean,
    modifier: Modifier = Modifier,
    onClick: () -> Unit = {},
    onLongClick: () -> Unit = {},
) {
    Surface(
        tonalElevation = 4.dp,
        shape = MaterialTheme.shapes.medium,
        modifier = modifier
            .clip(MaterialTheme.shapes.medium)
            .combinedClickable(
                onLongClick = onLongClick,
                onClick = onClick
            )
    ) {
        CardContent(
            imageUrl = imageUrl,
            name = name,
            type = type,
            isNsfw = isNsfw,
            showNsfw = showNsfw,
            blurStrength = blurStrength,
            blurHash = blurHash,
            shouldShowMedia = shouldShowMedia,
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FavoritesCreatorOptionsSheet(
    models: FavoriteModel.Creator,
    showSheet: Boolean,
    onDialogDismiss: () -> Unit,
    onNavigateToUser: (String) -> Unit,
) {
    if (showSheet) {
        val dao = koinInject<FavoritesDao>()
        val scope = rememberCoroutineScope()

        val sheetState = rememberModalBottomSheetState(
            skipPartiallyExpanded = true
        )

        val modelOptionsScope = rememberModelOptionsScope {
            item {
                Card(
                    onClick = {
                        onNavigateToUser(models.name)
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

            item {
                var showQrCode by remember { mutableStateOf(false) }

                if (showQrCode) {
                    ShareViaQrCode(
                        title = models.name,
                        url = "https://civitai.com/models/${models.id}",
                        qrCodeType = QrCodeType.User,
                        id = models.id.toString(),
                        username = models.name,
                        imageUrl = models.imageUrl.orEmpty(),
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
                val listDao = koinInject<ListDao>()
                val listState = rememberModalBottomSheetState(true)
                var showLists by remember { mutableStateOf(false) }
                if (showLists) {
                    val toaster = koinInject<ToasterState>()
                    ModalBottomSheet(
                        onDismissRequest = { showLists = false },
                        containerColor = MaterialTheme.colorScheme.surface,
                        sheetState = listState
                    ) {
                        ListChoiceScreen(
                            id = models.id,
                            onClick = { item ->
                                scope.launch {
                                    listDao.addToList(
                                        uuid = item.item.uuid,
                                        id = models.id,
                                        name = models.name,
                                        description = models.name,
                                        type = models.modelType,
                                        nsfw = false,
                                        imageUrl = models.imageUrl,
                                        favoriteType = FavoriteType.Model,
                                        hash = null
                                    )
                                    toaster.show("Added to List", type = ToastType.Success)
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
                Card(
                    onClick = { showLists = true },
                    shape = it
                ) {
                    ListItem(
                        leadingContent = { Icon(Icons.AutoMirrored.Filled.PlaylistAdd, null) },
                        headlineContent = { Text("Add to List") }
                    )
                }
            }

            item {
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
                        headlineContent = { Text("Unfavorite") }
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FavoritesModelOptionsSheet(
    models: FavoriteModel.Model,
    showSheet: Boolean,
    onDialogDismiss: () -> Unit,
    onNavigateToDetail: (Long) -> Unit,
) {
    if (showSheet) {
        val dao = koinInject<FavoritesDao>()
        val scope = rememberCoroutineScope()

        val sheetState = rememberModalBottomSheetState(
            skipPartiallyExpanded = true
        )

        val modelOptionsScope = rememberModelOptionsScope {
            item {
                Card(
                    onClick = {
                        onNavigateToDetail(models.id)
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

            item {
                var showQrCode by remember { mutableStateOf(false) }

                if (showQrCode) {
                    ShareViaQrCode(
                        title = models.name,
                        url = "https://civitai.com/models/${models.id}",
                        qrCodeType = QrCodeType.Model,
                        id = models.id.toString(),
                        username = "",
                        imageUrl = models.imageUrl.orEmpty(),
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
                val listDao = koinInject<ListDao>()
                val listState = rememberModalBottomSheetState(true)
                var showLists by remember { mutableStateOf(false) }
                if (showLists) {
                    ModalBottomSheet(
                        onDismissRequest = { showLists = false },
                        containerColor = MaterialTheme.colorScheme.surface,
                        sheetState = listState
                    ) {
                        ListChoiceScreen(
                            id = models.id,
                            onClick = { item ->
                                scope.launch {
                                    listDao.addToList(
                                        uuid = item.item.uuid,
                                        id = models.id,
                                        name = models.name,
                                        description = models.description,
                                        type = models.type,
                                        nsfw = models.nsfw,
                                        imageUrl = models.imageUrl,
                                        favoriteType = FavoriteType.Model,
                                        hash = models.hash
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
                Card(
                    onClick = { showLists = true },
                    shape = it
                ) {
                    ListItem(
                        leadingContent = { Icon(Icons.AutoMirrored.Filled.PlaylistAdd, null) },
                        headlineContent = { Text("Add to List") }
                    )
                }
            }

            item {
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
                        headlineContent = { Text("Unfavorite") }
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