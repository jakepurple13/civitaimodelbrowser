@file:Suppress("INLINE_FROM_HIGHER_PLATFORM")

package com.programmersbox.common

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.paging.CombinedLoadStates
import androidx.paging.LoadState
import com.programmersbox.common.components.LoadingImage
import kotlinx.coroutines.launch
import moe.tlaster.precompose.flow.collectAsStateWithLifecycle
import moe.tlaster.precompose.viewmodel.viewModel

@OptIn(ExperimentalFoundationApi::class, ExperimentalMaterial3Api::class)
@Composable
fun CivitAiScreen(
    network: Network,
) {
    val viewModel = viewModel { CivitAiViewModel(network) }
    val navController = LocalNavController.current
    val lazyPagingItems = viewModel.pager.collectAsLazyPagingItems()
    val scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior()
    val database by LocalDatabase.current.getFavorites().collectAsStateWithLifecycle(emptyList())
    val dataStore = LocalDataStore.current
    val showNsfw by remember { dataStore.showNsfw.flow }.collectAsStateWithLifecycle(false)
    val blurStrength by remember { dataStore.hideNsfwStrength.flow }.collectAsStateWithLifecycle(6f)

    val scope = rememberCoroutineScope()
    val lazyGridState = rememberLazyGridState()
    val pullToRefreshState = rememberPullRefreshState(
        refreshing = lazyPagingItems.loadState.refresh == LoadState.Loading,
        onRefresh = { lazyPagingItems.refresh() }
    )
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("CivitAi Model Browser") },
                navigationIcon = {
                    IconButton(
                        onClick = { lazyPagingItems.refresh() },
                    ) { Icon(Icons.Default.Refresh, null) }
                },
                actions = {
                    IconButton(
                        onClick = { navController.navigate(Screen.Settings.routeId) }
                    ) { Icon(Icons.Default.Settings, null) }

                    IconButton(
                        onClick = { navController.navigate(Screen.Favorites.routeId) }
                    ) { Icon(Icons.Default.Favorite, null) }
                },
                scrollBehavior = scrollBehavior
            )
        },
        bottomBar = {
            BottomAppBar(
                actions = {
                    Text("Models Loaded: ${lazyPagingItems.itemCount}")
                },
                floatingActionButton = {
                    FloatingActionButton(
                        onClick = { scope.launch { lazyGridState.animateScrollToItem(0) } },
                    ) { Icon(Icons.Default.ArrowUpward, null) }
                }
            )
        },
        modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection)
    ) { padding ->
        Box(
            modifier = Modifier
                .padding(padding)
                .pullRefresh(pullToRefreshState)
        ) {
            LazyVerticalGrid(
                state = lazyGridState,
                columns = adaptiveGridCell(),
                verticalArrangement = Arrangement.spacedBy(4.dp),
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                modifier = Modifier
                    .padding(4.dp)
                    .fillMaxSize()
            ) {
                if (lazyPagingItems.loadState.refresh == LoadState.Loading) {
                    item(
                        span = { GridItemSpan(maxLineSpan) }
                    ) {
                        Text(
                            text = "Waiting for items to load from the backend",
                            modifier = Modifier
                                .fillMaxWidth()
                                .wrapContentWidth(Alignment.CenterHorizontally)
                        )
                    }
                }

                items(
                    count = lazyPagingItems.itemCount,
                    contentType = lazyPagingItems.itemContentType(),
                    key = lazyPagingItems.itemKeyIndexed { model, index -> "${model.id}$index" }
                ) {
                    lazyPagingItems[it]?.let { models ->
                        ModelItem(
                            models = models,
                            onClick = {
                                navController.navigate(Screen.Detail.routeId.replace("{modelId}", models.id.toString()))
                            },
                            showNsfw = showNsfw,
                            blurStrength = blurStrength.dp,
                            isFavorite = database.any { m -> m.id == models.id },
                            modifier = Modifier.animateItemPlacement()
                        )
                    }
                }

                if (lazyPagingItems.loadState.append == LoadState.Loading) {
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

                if (lazyPagingItems.loadState.hasType<LoadState.Error>()) {
                    item(
                        span = { GridItemSpan(maxLineSpan) }
                    ) {
                        Column {
                            Text("Something went wrong! Please try again!")
                        }
                    }
                }
            }

            PullRefreshIndicator(
                refreshing = lazyPagingItems.loadState.refresh == LoadState.Loading || lazyPagingItems.loadState.append == LoadState.Loading,
                state = pullToRefreshState,
                modifier = Modifier.align(Alignment.TopCenter)
            )
        }
    }
}

inline fun <reified T : LoadState> CombinedLoadStates.hasType(): Boolean {
    return refresh == T::class || append == T::class || prepend == T::class
}

@Composable
private fun ModelItem(
    models: Models,
    showNsfw: Boolean,
    blurStrength: Dp,
    onClick: () -> Unit,
    isFavorite: Boolean,
    modifier: Modifier = Modifier,
) {
    val imageModel = remember { models.modelVersions.firstOrNull()?.images?.firstOrNull() }
    CoverCard(
        imageUrl = remember { imageModel?.url.orEmpty() },
        name = models.name,
        type = models.type,
        isNsfw = models.nsfw || imageModel?.nsfw != "None",
        showNsfw = showNsfw,
        blurStrength = blurStrength,
        onClick = onClick,
        isFavorite = isFavorite,
        modifier = modifier.size(
            width = ComposableUtils.IMAGE_WIDTH,
            height = ComposableUtils.IMAGE_HEIGHT
        )
    )
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun CoverCard(
    imageUrl: String,
    name: String,
    type: ModelType,
    isNsfw: Boolean,
    showNsfw: Boolean,
    blurStrength: Dp,
    isFavorite: Boolean,
    modifier: Modifier = Modifier,
    onClick: () -> Unit = {},
) {
    Surface(
        onClick = onClick,
        tonalElevation = 4.dp,
        shape = MaterialTheme.shapes.medium,
        border = if (isFavorite) BorderStroke(1.dp, MaterialTheme.colorScheme.primary) else null,
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

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun CardContent(
    imageUrl: String,
    name: String,
    type: ModelType,
    isNsfw: Boolean,
    showNsfw: Boolean,
    blurStrength: Dp,
) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
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
                label = { Text(type.name) },
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
                    border = AssistChipDefaults.assistChipBorder(
                        disabledBorderColor = MaterialTheme.colorScheme.error,
                        borderWidth = 1.dp
                    ),
                )
            }
        }
    }
}