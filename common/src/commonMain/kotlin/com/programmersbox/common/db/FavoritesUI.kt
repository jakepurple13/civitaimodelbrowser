@file:Suppress("INLINE_FROM_HIGHER_PLATFORM")

package com.programmersbox.common.db

import androidx.compose.animation.*
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.DialogProperties
import com.programmersbox.common.*
import com.programmersbox.common.home.CardContent
import dev.chrisbanes.haze.HazeState
import dev.chrisbanes.haze.haze
import dev.chrisbanes.haze.hazeChild
import io.kamel.image.KamelImage
import io.kamel.image.asyncPainterResource
import kotlinx.coroutines.launch
import moe.tlaster.precompose.flow.collectAsStateWithLifecycle
import moe.tlaster.precompose.viewmodel.viewModel

internal const val IMAGE_FILTER = "Image"
internal const val CREATOR_FILTER = "Creator"

@OptIn(ExperimentalFoundationApi::class, ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun FavoritesUI() {
    val hazeState = remember { HazeState() }
    val navController = LocalNavController.current
    val dataStore = LocalDataStore.current
    val scope = rememberCoroutineScope()
    val showNsfw by remember { dataStore.showNsfw.flow }.collectAsStateWithLifecycle(false)
    val blurStrength by remember { dataStore.hideNsfwStrength.flow }.collectAsStateWithLifecycle(6f)
    val database = LocalDatabase.current
    val lazyGridState = rememberLazyGridState()
    val viewModel = viewModel { FavoritesViewModel(database) }

    var showSortedByDialog by remember { mutableStateOf(false) }

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
                            windowInsets = WindowInsets(0.dp)
                        )

                        FlowRow(
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 4.dp)
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
            Column(
                modifier = Modifier.hazeChild(hazeState)
            ) {
                DockedSearchBar(
                    query = viewModel.search,
                    onQueryChange = { viewModel.search = it },
                    onSearch = {},
                    active = false,
                    onActiveChange = {},
                    leadingIcon = {
                        IconButton(
                            onClick = { navController.popBackStack() }
                        ) { Icon(Icons.Default.ArrowBack, null) }
                    },
                    placeholder = { Text("Search Favorites") },
                    trailingIcon = {
                        Row(
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("(${viewModel.favoritesList.size})")
                            IconButton(
                                onClick = { showSortedByDialog = true }
                            ) { Icon(Icons.Default.Sort, null) }
                            AnimatedVisibility(viewModel.search.isNotEmpty()) {
                                IconButton(
                                    onClick = { viewModel.search = "" }
                                ) { Icon(Icons.Default.Clear, null) }
                            }
                        }
                    },
                    colors = SearchBarDefaults.colors(
                        containerColor = Color.Transparent
                    ),
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
                .haze(
                    state = hazeState,
                    backgroundColor = MaterialTheme.colorScheme.surface
                )
                .fillMaxSize()
        ) {
            items(
                viewModel.viewingList,
                key = {
                    when (it) {
                        is FavoriteModel.Creator -> it.name
                        is FavoriteModel.Image -> it.imageUrl
                        is FavoriteModel.Model -> it.id
                    } as Any
                }
            ) { model ->
                when (model) {
                    is FavoriteModel.Creator -> {
                        CreatorItem(
                            models = model,
                            onClick = { navController.navigateToUser(model.name) }
                        )
                    }

                    is FavoriteModel.Image -> {
                        var sheetDetails by remember { mutableStateOf<FavoriteModel.Image?>(null) }

                        sheetDetails?.let { sheetModel ->
                            SheetDetails(
                                onDismiss = { sheetDetails = null },
                                content = {
                                    SheetContent(
                                        image = sheetModel,
                                        onNavigate = {
                                            sheetDetails = null
                                            navController.navigateToDetail(sheetModel.modelId)
                                        },
                                        onRemoveFavorite = {
                                            scope.launch {
                                                database.removeFrom {
                                                    removeIf { f ->
                                                        f.imageUrl == sheetModel.imageUrl &&
                                                                f.favoriteType == FavoriteType.Image.name
                                                    }
                                                }
                                            }
                                        }
                                    )
                                }
                            )
                        }
                        ImageItem(
                            models = model,
                            onClick = { sheetDetails = model },
                            showNsfw = showNsfw,
                            blurStrength = blurStrength.dp,
                            modifier = Modifier.animateItemPlacement()
                        )
                    }

                    is FavoriteModel.Model -> {
                        ModelItem(
                            models = model,
                            onClick = { navController.navigateToDetail(model.id) },
                            showNsfw = showNsfw,
                            blurStrength = blurStrength.dp,
                            modifier = Modifier.animateItemPlacement()
                        )
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class, ExperimentalMaterial3Api::class)
@Composable
private fun SheetContent(
    image: FavoriteModel.Image,
    onRemoveFavorite: () -> Unit,
    onNavigate: () -> Unit,
) {
    val painter = asyncPainterResource(image.imageUrl.orEmpty())
    SelectionContainer {
        var imagePopup by remember { mutableStateOf(false) }

        if (imagePopup) {
            val sroState = rememberSROState()
            AlertDialog(
                properties = DialogProperties(usePlatformDefaultWidth = false),
                onDismissRequest = { imagePopup = false },
                title = {
                    TopAppBar(
                        title = {},
                        actions = {
                            IconButton(
                                onClick = sroState::reset
                            ) { Icon(Icons.Default.Refresh, null) }
                        },
                        windowInsets = WindowInsets(0.dp)
                    )
                },
                text = {
                    KamelImage(
                        resource = painter,
                        onLoading = {
                            CircularProgressIndicator(
                                progress = animateFloatAsState(
                                    targetValue = it,
                                    label = ""
                                ).value
                            )
                        },
                        contentDescription = null,
                        modifier = Modifier.scaleRotateOffsetReset(sroState)
                    )
                },
                confirmButton = { TextButton(onClick = { imagePopup = false }) { Text("Done") } }
            )
        }
        Column(
            modifier = Modifier.verticalScroll(rememberScrollState())
        ) {
            TopAppBar(
                title = {},
                navigationIcon = {
                    IconButton(onClick = onRemoveFavorite) {
                        Icon(Icons.Default.Favorite, null)
                    }
                },
                windowInsets = WindowInsets(0.dp),
                actions = {
                    TextButton(
                        onClick = onNavigate
                    ) {
                        Text("View Model")
                        Icon(Icons.Default.ArrowRightAlt, null)
                    }
                }
            )
            KamelImage(
                resource = painter,
                onLoading = {
                    CircularProgressIndicator(
                        progress = animateFloatAsState(
                            targetValue = it,
                            label = ""
                        ).value
                    )
                },
                contentDescription = null,
                modifier = Modifier
                    .combinedClickable(onDoubleClick = { imagePopup = true }) {}
                    .align(Alignment.CenterHorizontally)
            )
            if (image.nsfw) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    modifier = Modifier.padding(horizontal = 4.dp)
                ) {
                    ElevatedAssistChip(
                        label = { Text("NSFW") },
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
            image.imageMetaDb?.let { meta ->
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
    }
}

@Composable
private fun CreatorItem(
    models: FavoriteModel.Creator,
    onClick: () -> Unit,
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
    modifier: Modifier = Modifier,
) {
    CoverCard(
        imageUrl = models.imageUrl.orEmpty(),
        name = models.name,
        type = IMAGE_FILTER,
        isNsfw = models.nsfw,
        showNsfw = showNsfw,
        blurStrength = blurStrength,
        onClick = onClick,
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
            blurStrength = blurStrength
        )
    }
}