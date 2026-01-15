package com.programmersbox.common.presentation.home

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyGridScope
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Sort
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.Block
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.AppBarRow
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.BottomAppBarDefaults
import androidx.compose.material3.Button
import androidx.compose.material3.CircularWavyProgressIndicator
import androidx.compose.material3.ElevatedAssistChip
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.pulltorefresh.PullToRefreshDefaults
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.paging.CombinedLoadStates
import androidx.paging.LoadState
import androidx.paging.compose.LazyPagingItems
import androidx.paging.compose.collectAsLazyPagingItems
import androidx.paging.compose.itemContentType
import chaintech.videoplayer.ui.preview.VideoPreviewComposable
import com.programmersbox.common.CivitSort
import com.programmersbox.common.ComposableUtils
import com.programmersbox.common.DataStore
import com.programmersbox.common.ModelType
import com.programmersbox.common.Models
import com.programmersbox.common.NetworkConnectionRepository
import com.programmersbox.common.WindowedScaffold
import com.programmersbox.common.adaptiveGridCell
import com.programmersbox.common.db.BlacklistedItemRoom
import com.programmersbox.common.db.FavoriteModel
import com.programmersbox.common.db.FavoritesDao
import com.programmersbox.common.isScrollingUp
import com.programmersbox.common.paging.itemKeyIndexed
import com.programmersbox.common.presentation.components.CivitBottomBar
import com.programmersbox.common.presentation.components.CivitRail
import com.programmersbox.common.presentation.components.LoadingImage
import com.programmersbox.common.presentation.components.ModelOptionsSheet
import com.programmersbox.common.showRefreshButton
import dev.chrisbanes.haze.HazeProgressive
import dev.chrisbanes.haze.LocalHazeStyle
import dev.chrisbanes.haze.hazeEffect
import dev.chrisbanes.haze.hazeSource
import dev.chrisbanes.haze.rememberHazeState
import kotlinx.coroutines.launch
import org.koin.compose.koinInject
import org.koin.compose.viewmodel.koinViewModel

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun CivitAiScreen(
    onNavigateToDetail: (String) -> Unit,
    onNavigateToQrCode: () -> Unit,
    onNavigateToUser: (String) -> Unit,
    onNavigateToDetailImages: (Long, String) -> Unit,
    onNavigateToBlacklist: () -> Unit,
    onNavigateToImages: () -> Unit,
    onNavigateToSearch: () -> Unit,
    viewModel: CivitAiViewModel = koinViewModel(),
) {
    val connectionRepository = koinInject<NetworkConnectionRepository>()
    val shouldShowMedia by remember { derivedStateOf { connectionRepository.shouldShowMedia } }
    val db = koinInject<FavoritesDao>()
    val dataStore = koinInject<DataStore>()
    val database by db
        .getFavoriteModels()
        .collectAsStateWithLifecycle(emptyList())

    val blacklisted by db
        .getBlacklisted()
        .collectAsStateWithLifecycle(emptyList())
    val showBlur by dataStore.rememberShowBlur()
    val useProgressive by dataStore.rememberUseProgressive()
    val showNsfw by dataStore.showNsfw()
    val blurStrength by dataStore.hideNsfwStrength()
    val lazyPagingItems = viewModel.pager.collectAsLazyPagingItems()

    val hazeState = rememberHazeState(showBlur)
    val scope = rememberCoroutineScope()
    val lazyGridState = rememberLazyGridState()
    val pullToRefreshState = rememberPullToRefreshState()

    val hazeStyle = LocalHazeStyle.current

    val bottomBarScrollBehavior = BottomAppBarDefaults.exitAlwaysScrollBehavior()

    WindowedScaffold(
        topBar = {
            CivitTopBar(
                showBlur = showBlur,
                onNavigateToSearch = onNavigateToSearch,
                onNavigateToQrCode = onNavigateToQrCode,
                onNavigateToImages = onNavigateToImages,
                onNavigateToBlacklist = onNavigateToBlacklist,
                sort = viewModel.sort,
                onSortChange = { viewModel.sort = it },
                onRefresh = lazyPagingItems::refresh,
                modifier = Modifier.hazeEffect(hazeState, hazeStyle) {
                    progressive = if (useProgressive)
                        HazeProgressive.verticalGradient(
                            startIntensity = 1f,
                            endIntensity = 0f,
                            preferPerformance = true
                        )
                    else
                        null
                }
            )
        },
        rail = { CivitRail() },
        bottomBar = {
            CivitBottomBar(
                showBlur = showBlur,
                bottomBarScrollBehavior = bottomBarScrollBehavior,
                modifier = Modifier.hazeEffect(hazeState, hazeStyle) {
                    progressive = if (useProgressive)
                        HazeProgressive.verticalGradient(
                            startIntensity = 0f,
                            endIntensity = 1f,
                            preferPerformance = true
                        )
                    else
                        null
                }
            )
        },
        floatingActionButton = {
            AnimatedVisibility(
                visible = lazyGridState.isScrollingUp(),
                enter = fadeIn() + slideInHorizontally { it },
                exit = slideOutHorizontally { it } + fadeOut()
            ) {
                FloatingActionButton(
                    onClick = { scope.launch { lazyGridState.animateScrollToItem(0) } },
                ) { Icon(Icons.Default.ArrowUpward, null) }
            }
        },
        modifier = Modifier.nestedScroll(bottomBarScrollBehavior.nestedScrollConnection)
    ) { padding ->
        PullToRefreshBox(
            isRefreshing = lazyPagingItems.loadState.refresh == LoadState.Loading
                    || lazyPagingItems.loadState.append == LoadState.Loading,
            state = pullToRefreshState,
            onRefresh = lazyPagingItems::refresh,
            indicator = {
                PullToRefreshDefaults.LoadingIndicator(
                    isRefreshing = lazyPagingItems.loadState.refresh == LoadState.Loading
                            || lazyPagingItems.loadState.append == LoadState.Loading,
                    state = pullToRefreshState,
                    modifier = Modifier
                        .align(Alignment.TopCenter)
                        .padding(padding)
                )
            },
            modifier = Modifier.fillMaxSize()
        ) {
            LazyVerticalGrid(
                state = lazyGridState,
                contentPadding = padding,
                columns = adaptiveGridCell(),
                verticalArrangement = Arrangement.spacedBy(4.dp),
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                modifier = Modifier
                    .hazeSource(state = hazeState)
                    .fillMaxSize()
            ) {
                modelItems(
                    lazyPagingItems = lazyPagingItems,
                    onNavigateToDetail = onNavigateToDetail,
                    showNsfw = showNsfw,
                    blurStrength = blurStrength,
                    database = database,
                    blacklisted = blacklisted,
                    shouldShowMedia = shouldShowMedia,
                    onNavigateToUser = onNavigateToUser,
                    onNavigateToDetailImages = onNavigateToDetailImages,
                )
            }
        }
    }
}

@OptIn(
    ExperimentalFoundationApi::class, ExperimentalMaterial3Api::class,
    ExperimentalMaterial3ExpressiveApi::class
)
fun LazyGridScope.modelItems(
    lazyPagingItems: LazyPagingItems<Models>,
    onNavigateToDetail: (String) -> Unit,
    showNsfw: Boolean,
    blurStrength: Float,
    database: List<FavoriteModel>,
    blacklisted: List<BlacklistedItemRoom>,
    shouldShowMedia: Boolean,
    onNavigateToUser: ((String) -> Unit)? = null,
    onNavigateToDetailImages: ((Long, String) -> Unit)? = null,
) {
    items(
        count = lazyPagingItems.itemCount,
        contentType = lazyPagingItems.itemContentType { "model" },
        key = lazyPagingItems.itemKeyIndexed { model, index -> "${model.id}$index" }
    ) {
        lazyPagingItems[it]?.let { models ->
            val isBlacklisted = blacklisted.any { b -> b.id == models.id }
            var showSheet by remember { mutableStateOf(false) }

            ModelOptionsSheet(
                models = models,
                blacklisted = blacklisted,
                isBlacklisted = isBlacklisted,
                showSheet = showSheet,
                onDialogDismiss = { showSheet = false },
                onNavigateToDetail = onNavigateToDetail,
                onNavigateToUser = onNavigateToUser,
                onNavigateToDetailImages = onNavigateToDetailImages,
            )

            ModelItem(
                models = models,
                onClick = { onNavigateToDetail(models.id.toString()) },
                onLongClick = { showSheet = true },
                showNsfw = showNsfw,
                blurStrength = blurStrength.dp,
                shouldShowMedia = shouldShowMedia,
                isFavorite = database
                    .filterIsInstance<FavoriteModel.Model>()
                    .any { m -> m.id == models.id },
                isBlacklisted = isBlacklisted,
                checkIfImageUrlIsBlacklisted = { url ->
                    blacklisted.none { b -> b.imageUrl == url }
                },
                modifier = Modifier.animateItem()
            )
        }
    }

    if (lazyPagingItems.loadState.hasType<LoadState.Loading>()) {
        item(
            span = { GridItemSpan(maxLineSpan) }
        ) {
            CircularWavyProgressIndicator(
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
                error.error.message?.let { Text(it, textAlign = TextAlign.Center) }
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
    shouldShowMedia: Boolean,
    checkIfImageUrlIsBlacklisted: (String) -> Boolean,
    modifier: Modifier = Modifier,
) {
    val imageModel = remember {
        models.modelVersions.firstNotNullOfOrNull { mv ->
            mv.images.firstOrNull { it.url.isNotEmpty() && checkIfImageUrlIsBlacklisted(it.url) }
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
        shouldShowMedia = shouldShowMedia,
        blurHash = imageModel?.hash,
        creatorImage = models.creator?.image,
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
    shouldShowMedia: Boolean,
    modifier: Modifier = Modifier,
    blurHash: String? = null,
    creatorImage: String? = null,
    onLongClick: () -> Unit = {},
    onClick: () -> Unit = {},
) {
    Surface(
        tonalElevation = 4.dp,
        shape = MaterialTheme.shapes.medium,
        border = if (isFavorite)
            BorderStroke(1.dp, MaterialTheme.colorScheme.primary)
        else
            null,
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
            blurStrength = blurStrength,
            blurHash = blurHash,
            shouldShowMedia = shouldShowMedia,
            creatorImage = creatorImage,
        )
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun CardContent(
    imageUrl: String,
    creatorImage: String?,
    name: String,
    type: String,
    isNsfw: Boolean,
    showNsfw: Boolean,
    blurStrength: Dp,
    shouldShowMedia: Boolean,
    isBlacklisted: Boolean = false,
    blurHash: String? = null,
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
            if (imageUrl.endsWith("mp4") && shouldShowMedia) {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .background(Color.Black)
                        .matchParentSize()
                ) {
                    VideoPreviewComposable(
                        url = imageUrl,
                        frameCount = 5,
                        contentScale = ContentScale.Crop
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
                    imageUrl = imageUrl,
                    isNsfw = isNsfw,
                    name = name,
                    hash = blurHash,
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
            itemVerticalAlignment = Alignment.CenterVertically,
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

            creatorImage?.let { image ->
                LoadingImage(
                    image,
                    contentScale = ContentScale.FillBounds,
                    modifier = Modifier
                        .size(32.dp)
                        .clip(CircleShape)
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CivitTopBar(
    onNavigateToSearch: () -> Unit,
    onNavigateToQrCode: () -> Unit,
    onNavigateToImages: () -> Unit,
    onNavigateToBlacklist: () -> Unit,
    onRefresh: () -> Unit,
    showBlur: Boolean,
    sort: CivitSort,
    onSortChange: (CivitSort) -> Unit,
    modifier: Modifier = Modifier,
) {
    val topAppBarColors = TopAppBarDefaults.topAppBarColors(
        containerColor = if (showBlur)
            Color.Transparent
        else
            MaterialTheme.colorScheme.surface,
        scrolledContainerColor = if (showBlur)
            Color.Transparent
        else
            MaterialTheme.colorScheme.surface,
    )

    var showSortBy by rememberSaveable { mutableStateOf(false) }

    Column(
        modifier = modifier
    ) {
        TopAppBar(
            title = { Text("CivitAI") },
            navigationIcon = {
                Row {
                    IconButton(
                        onClick = onNavigateToSearch
                    ) { Icon(Icons.Default.Search, null) }
                    if (showRefreshButton) {
                        IconButton(
                            onClick = onRefresh
                        ) { Icon(Icons.Default.Refresh, null) }
                    }
                }
            },
            actions = {
                AppBarRow(
                    maxItemCount = 3
                ) {
                    clickableItem(
                        onClick = onNavigateToQrCode,
                        icon = { Icon(Icons.Default.QrCodeScanner, null) },
                        label = "QR Code Scanner"
                    )
                    toggleableItem(
                        checked = showSortBy,
                        onCheckedChange = { showSortBy = it },
                        icon = { Icon(Icons.AutoMirrored.Filled.Sort, null) },
                        label = "Sort"
                    )
                    clickableItem(
                        onClick = onNavigateToImages,
                        icon = { Icon(Icons.Default.Image, null) },
                        label = "Images"
                    )
                    clickableItem(
                        onClick = onNavigateToBlacklist,
                        icon = { Icon(Icons.Default.Block, null) },
                        label = "Blacklisted"
                    )
                }
            },
            colors = topAppBarColors,
        )

        AnimatedVisibility(showSortBy) {
            SingleChoiceSegmentedButtonRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
            ) {
                CivitSort.entries.forEachIndexed { index, searchType ->
                    SegmentedButton(
                        selected = searchType == sort,
                        onClick = { onSortChange(searchType) },
                        shape = SegmentedButtonDefaults.itemShape(
                            index = index,
                            count = CivitSort.entries.size
                        ),
                        label = { Text(searchType.visualName) }
                    )
                }
            }
        }
    }
}


@Composable
fun BlacklistHandling(
    blacklisted: List<BlacklistedItemRoom>,
    modelId: Long,
    name: String,
    nsfw: Boolean,
    showDialog: Boolean,
    onDialogDismiss: () -> Unit,
    imageUrl: String? = null,
) {
    val db = koinInject<FavoritesDao>()
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
                                    ?.let { db.delete(it) }
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

/*
@Composable
fun BlacklistHandling(
    isBlacklisted: Boolean,
    imageUrl: String?,
    name: String,
    nsfw: Boolean,
    showDialog: Boolean,
    onDialogDismiss: () -> Unit,
) {
    val db = koinInject<FavoritesDao>()
    val scope = rememberCoroutineScope()

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
                               db.delete()
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
}*/
