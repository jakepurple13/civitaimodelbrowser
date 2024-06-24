@file:Suppress("INLINE_FROM_HIGHER_PLATFORM")

package com.programmersbox.common.home

import androidx.compose.animation.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyGridScope
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.paging.CombinedLoadStates
import androidx.paging.LoadState
import com.programmersbox.common.*
import com.programmersbox.common.components.LoadingImage
import com.programmersbox.common.components.PullRefreshIndicator
import com.programmersbox.common.components.pullRefresh
import com.programmersbox.common.components.rememberPullRefreshState
import com.programmersbox.common.db.BlacklistedItem
import com.programmersbox.common.db.FavoriteModel
import com.programmersbox.common.paging.LazyPagingItems
import com.programmersbox.common.paging.collectAsLazyPagingItems
import com.programmersbox.common.paging.itemContentType
import com.programmersbox.common.paging.itemKeyIndexed
import dev.chrisbanes.haze.HazeState
import dev.chrisbanes.haze.haze
import dev.chrisbanes.haze.hazeChild
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CivitAiScreen(
    network: Network = LocalNetwork.current,
) {
    val hazeState = remember { HazeState() }
    val navController = LocalNavController.current
    val db = LocalDatabase.current
    val database by db.getFavorites().collectAsStateWithLifecycle(emptyList())
    val blacklisted by db.getBlacklistedItems().collectAsStateWithLifecycle(emptyList())
    val dataStore = LocalDataStore.current
    val showBlur by dataStore.rememberShowBlur()
    val showNsfw by remember { dataStore.showNsfw.flow }.collectAsStateWithLifecycle(false)
    val blurStrength by remember { dataStore.hideNsfwStrength.flow }.collectAsStateWithLifecycle(6f)
    val viewModel = viewModel { CivitAiViewModel(network, dataStore) }
    val lazyPagingItems = viewModel.pager.collectAsLazyPagingItems()

    val scope = rememberCoroutineScope()
    val lazyGridState = rememberLazyGridState()
    val pullToRefreshState = rememberPullRefreshState(
        refreshing = lazyPagingItems.loadState.refresh == LoadState.Loading,
        onRefresh = { lazyPagingItems.refresh() }
    )

    val searchViewModel = viewModel { CivitAiSearchViewModel(network, dataStore) }

    LaunchedEffect(searchViewModel.showSearch) {
        if (!searchViewModel.showSearch) {
            searchViewModel.searchQuery = ""
            searchViewModel.onSearch("")
        }
    }

    Scaffold(
        topBar = {
            SearchAppBar(
                viewModel = searchViewModel,
                database = database.filterIsInstance<FavoriteModel.Model>(),
                showNsfw = showNsfw,
                blurStrength = blurStrength,
                blacklisted = blacklisted,
                onShowSearch = { searchViewModel.showSearch = it },
                showBlur = showBlur,
                modifier = Modifier.ifTrue(showBlur) { hazeChild(hazeState) }
            )
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
        Box(
            modifier = Modifier.pullRefresh(pullToRefreshState)
        ) {
            LazyVerticalGrid(
                state = lazyGridState,
                contentPadding = padding,
                columns = adaptiveGridCell(),
                verticalArrangement = Arrangement.spacedBy(4.dp),
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                modifier = Modifier
                    .ifTrue(showBlur) { haze(state = hazeState) }
                    .fillMaxSize()
            ) {
                modelItems(
                    lazyPagingItems = lazyPagingItems,
                    navController = navController,
                    showNsfw = showNsfw,
                    blurStrength = blurStrength,
                    database = database.filterIsInstance<FavoriteModel.Model>(),
                    blacklisted = blacklisted,
                )
            }

            /*VerticalScrollbar(
                rememberScrollbarAdapter(lazyGridState),
                modifier = Modifier
                    .align(Alignment.CenterEnd)
                    .fillMaxHeight(),
            )*/

            PullRefreshIndicator(
                refreshing = lazyPagingItems.loadState.refresh == LoadState.Loading || lazyPagingItems.loadState.append == LoadState.Loading,
                state = pullToRefreshState,
                modifier = Modifier
                    .padding(padding)
                    .align(Alignment.TopCenter)
            )
        }
    }

    /*SearchView(
        viewModel = searchViewModel,
        database = database.filterIsInstance<FavoriteModel.Model>(),
        showNsfw = showNsfw,
        blurStrength = blurStrength,
        blacklisted = blacklisted,
    )*/
}

@OptIn(ExperimentalFoundationApi::class)
fun LazyGridScope.modelItems(
    lazyPagingItems: LazyPagingItems<Models>,
    navController: NavController,
    showNsfw: Boolean,
    blurStrength: Float,
    database: List<FavoriteModel>,
    blacklisted: List<BlacklistedItem>,
) {
    items(
        count = lazyPagingItems.itemCount,
        contentType = lazyPagingItems.itemContentType(),
        key = lazyPagingItems.itemKeyIndexed { model, index -> "${model.id}$index" }
    ) {
        lazyPagingItems[it]?.let { models ->
            val isBlacklisted = blacklisted.any { b -> b.id == models.id }
            var showDialog by remember { mutableStateOf(false) }

            BlacklistHandling(
                blacklisted = blacklisted,
                modelId = models.id,
                name = models.name,
                nsfw = models.nsfw,
                showDialog = showDialog,
                onDialogDismiss = { showDialog = false }
            )

            ContextMenu(
                isBlacklisted = isBlacklisted,
                blacklistItems = blacklisted,
                modelId = models.id,
                name = models.name,
                nsfw = models.nsfw,
                imageUrl = null
            ) {
                ModelItem(
                    models = models,
                    onClick = { navController.navigateToDetail(models.id) },
                    onLongClick = { showDialog = true },
                    showNsfw = showNsfw,
                    blurStrength = blurStrength.dp,
                    isFavorite = database.any { m -> m.id == models.id },
                    isBlacklisted = isBlacklisted,
                    modifier = Modifier.animateItemPlacement()
                )
            }
        }
    }

    if (lazyPagingItems.loadState.hasType<LoadState.Loading>()) {
        item(
            span = { GridItemSpan(maxLineSpan) }
        ) {
            CircularProgressIndicator(
                modifier = Modifier
                    .fillMaxWidth()
                    .wrapContentWidth(Alignment.CenterHorizontally)
            )
        }
    }

    lazyPagingItems.loadState.getType<LoadState.Error>()?.let { error ->
        item(
            span = { GridItemSpan(maxLineSpan) }
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text("Something went wrong")
                Button(
                    onClick = lazyPagingItems::retry
                ) { Text("Try Again") }
                error.error.message?.let { Text(it) }
            }
        }
    }
}

inline fun <reified T : LoadState> CombinedLoadStates.hasType(): Boolean {
    return refresh is T || append is T || prepend is T
}

inline fun <reified T : LoadState> CombinedLoadStates.getType(): T? {
    return refresh as? T ?: append as? T ?: prepend as? T
}

@Composable
private fun ModelItem(
    models: Models,
    showNsfw: Boolean,
    blurStrength: Dp,
    onClick: () -> Unit,
    onLongClick: () -> Unit,
    isFavorite: Boolean,
    isBlacklisted: Boolean,
    modifier: Modifier = Modifier,
) {
    val imageModel = remember {
        models.modelVersions.firstNotNullOfOrNull { mv ->
            mv.images.firstOrNull { it.url.isNotEmpty() }
        }
    }
    CoverCard(
        imageUrl = remember { imageModel?.url.orEmpty() },
        name = models.name,
        type = models.type,
        isNsfw = models.nsfw || imageModel?.nsfw?.canNotShow() == true,
        showNsfw = showNsfw,
        blurStrength = blurStrength,
        onClick = onClick,
        isFavorite = isFavorite,
        isBlacklisted = isBlacklisted,
        onLongClick = onLongClick,
        modifier = modifier.size(
            width = ComposableUtils.IMAGE_WIDTH,
            height = ComposableUtils.IMAGE_HEIGHT
        )
    )
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun CoverCard(
    imageUrl: String,
    name: String,
    type: ModelType,
    isNsfw: Boolean,
    showNsfw: Boolean,
    blurStrength: Dp,
    isFavorite: Boolean,
    isBlacklisted: Boolean,
    modifier: Modifier = Modifier,
    onLongClick: () -> Unit = {},
    onClick: () -> Unit = {},
) {
    Surface(
        tonalElevation = 4.dp,
        shape = MaterialTheme.shapes.medium,
        border = if (isFavorite) BorderStroke(1.dp, MaterialTheme.colorScheme.primary) else null,
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
            type = type.name,
            isNsfw = isNsfw,
            isBlacklisted = isBlacklisted,
            showNsfw = showNsfw,
            blurStrength = blurStrength
        )
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun CardContent(
    imageUrl: String,
    name: String,
    type: String,
    isNsfw: Boolean,
    showNsfw: Boolean,
    blurStrength: Dp,
    isBlacklisted: Boolean = false,
) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        if (isBlacklisted) {
            Box(
                Modifier
                    .background(Color.Black)
                    .matchParentSize()
            )
        } else {
            LoadingImage(
                imageUrl = imageUrl,
                isNsfw = isNsfw,
                name = name,
                modifier = Modifier
                    .matchParentSize()
                    .let {
                        if (!showNsfw && isNsfw) {
                            it.blur(blurStrength)
                        } else {
                            it
                        }
                    },
            )
        }

        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            Color.Transparent,
                            Color.Black
                        ),
                        startY = 50f
                    )
                )
        ) {
            Text(
                name,
                style = MaterialTheme
                    .typography
                    .bodyLarge
                    .copy(textAlign = TextAlign.Center, color = Color.White),
                maxLines = 2,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 4.dp)
                    .align(Alignment.BottomCenter)
            )
        }

        FlowRow(
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 4.dp)
                .align(Alignment.TopCenter)
        ) {
            ElevatedAssistChip(
                label = { Text(type) },
                onClick = {},
                colors = AssistChipDefaults.elevatedAssistChipColors(
                    disabledLabelColor = MaterialTheme.colorScheme.onSurface,
                    disabledContainerColor = MaterialTheme.colorScheme.surface
                ),
                enabled = false,
            )

            if (isNsfw) {
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
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SearchAppBar(
    viewModel: CivitAiSearchViewModel,
    database: List<FavoriteModel>,
    blacklisted: List<BlacklistedItem>,
    showNsfw: Boolean,
    blurStrength: Float,
    onShowSearch: (Boolean) -> Unit,
    showBlur: Boolean,
    modifier: Modifier = Modifier,
) {
    val navController = LocalNavController.current
    val lazyPagingItems = viewModel.pager.collectAsLazyPagingItems()
    SearchBar(
        query = viewModel.searchQuery,
        onQueryChange = { viewModel.searchQuery = it },
        onSearch = viewModel::onSearch,
        active = viewModel.showSearch,
        onActiveChange = { viewModel.showSearch = it },
        placeholder = { Text("Search CivitAi") },
        trailingIcon = {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                AnimatedVisibility(
                    viewModel.searchQuery.isNotEmpty(),
                    enter = slideInHorizontally { it } + fadeIn(),
                    exit = slideOutHorizontally { it } + fadeOut()
                ) {
                    IconButton(
                        onClick = {
                            viewModel.searchQuery = ""
                            viewModel.onSearch("")
                        }
                    ) { Icon(Icons.Default.Clear, null) }
                }

                if (showRefreshButton) {
                    IconButton(
                        onClick = { onShowSearch(true) }
                    ) { Icon(Icons.Default.Search, null) }
                }

                IconButton(
                    onClick = { navController.navigate(Screen.Settings.routeId) }
                ) { Icon(Icons.Default.Settings, null) }

                IconButton(
                    onClick = { navController.navigate(Screen.Favorites.routeId) }
                ) { Icon(Icons.Default.Favorite, null) }
            }
        },
        leadingIcon = {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (showRefreshButton) {
                    IconButton(
                        onClick = { lazyPagingItems.refresh() },
                    ) { Icon(Icons.Default.Refresh, null) }
                }

                AnimatedContent(
                    viewModel.showSearch,
                    transitionSpec = {
                        slideInHorizontally { -it } + fadeIn() togetherWith slideOutHorizontally { -it } + fadeOut()
                    },
                    contentAlignment = Alignment.Center
                ) { target ->
                    if (target) {
                        IconButton(
                            onClick = { viewModel.showSearch = false }
                        ) { Icon(Icons.AutoMirrored.Filled.ArrowBack, null) }
                    } else {
                        Icon(Icons.Default.Search, null)
                    }
                }
            }
        },
        colors = if (showBlur) SearchBarDefaults.colors(
            containerColor = Color.Transparent
        ) else SearchBarDefaults.colors(),
        modifier = modifier.fillMaxWidth()
    ) {
        LazyVerticalGrid(
            columns = adaptiveGridCell(),
            verticalArrangement = Arrangement.spacedBy(4.dp),
            horizontalArrangement = Arrangement.spacedBy(4.dp),
            modifier = Modifier
                .padding(4.dp)
                .fillMaxSize()
        ) {
            modelItems(
                lazyPagingItems = lazyPagingItems,
                navController = navController,
                showNsfw = showNsfw,
                blurStrength = blurStrength,
                database = database,
                blacklisted = blacklisted,
            )
        }
    }
}

@Composable
fun BlacklistHandling(
    blacklisted: List<BlacklistedItem>,
    modelId: Long,
    name: String,
    nsfw: Boolean,
    imageUrl: String? = null,
    showDialog: Boolean,
    onDialogDismiss: () -> Unit,
) {
    val db = LocalDatabase.current
    val scope = rememberCoroutineScope()
    val isBlacklisted = blacklisted.any { b -> b.id == modelId }

    if (showDialog) {
        AlertDialog(
            onDismissRequest = onDialogDismiss,
            title = { Text(if (isBlacklisted) "Remove from Blacklist?" else "Add to Blacklist?") },
            text = {
                Text(if (isBlacklisted) "See the model again!" else "Black out the image.")
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        scope.launch {
                            if (isBlacklisted) {
                                blacklisted.find { b -> b.id == modelId }
                                    ?.let { db.removeBlacklistItem(it) }
                            } else {
                                db.blacklistItem(modelId, name, nsfw, imageUrl)
                            }
                            onDialogDismiss()
                        }
                    }
                ) { Text("Confirm") }
            },
            dismissButton = {
                TextButton(
                    onClick = onDialogDismiss
                ) { Text("Dismiss") }
            }
        )
    }
}