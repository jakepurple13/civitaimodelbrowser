package com.programmersbox.common.presentation.details

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.basicMarquee
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.selection.toggleable
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.PlaylistAdd
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.OpenInBrowser
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.AppBarRow
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.BottomAppBar
import androidx.compose.material3.BottomAppBarDefaults
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.CircularWavyProgressIndicator
import androidx.compose.material3.ElevatedAssistChip
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.FabPosition
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.FloatingToolbarDefaults
import androidx.compose.material3.FloatingToolbarDefaults.floatingToolbarVerticalNestedScroll
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.HorizontalFloatingToolbar
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import chaintech.videoplayer.ui.preview.VideoPreviewComposable
import com.programmersbox.common.BackButton
import com.programmersbox.common.ComposableUtils
import com.programmersbox.common.ContextMenu
import com.programmersbox.common.DataStore
import com.programmersbox.common.ModelImage
import com.programmersbox.common.NetworkConnectionRepository
import com.programmersbox.common.adaptiveGridCell
import com.programmersbox.common.db.FavoriteType
import com.programmersbox.common.db.FavoritesDao
import com.programmersbox.common.db.ListDao
import com.programmersbox.common.presentation.components.ImageSheet
import com.programmersbox.common.presentation.components.ListChoiceScreen
import com.programmersbox.common.presentation.components.LoadingImage
import com.programmersbox.common.presentation.components.ToastType
import com.programmersbox.common.presentation.components.ToasterState
import com.programmersbox.common.presentation.home.BlacklistHandling
import com.programmersbox.common.presentation.qrcode.QrCodeType
import com.programmersbox.common.presentation.qrcode.ShareViaQrCode
import dev.chrisbanes.haze.HazeProgressive
import dev.chrisbanes.haze.HazeState
import dev.chrisbanes.haze.HazeStyle
import dev.chrisbanes.haze.LocalHazeStyle
import dev.chrisbanes.haze.hazeEffect
import dev.chrisbanes.haze.hazeSource
import dev.chrisbanes.haze.rememberHazeState
import kotlinx.coroutines.launch
import org.koin.compose.koinInject
import org.koin.compose.viewmodel.koinViewModel

@OptIn(
    ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class,
    ExperimentalMaterial3ExpressiveApi::class
)
@Composable
fun CivitAiDetailScreen(
    id: String?,
    onNavigateToUser: (String) -> Unit,
    onNavigateToDetailImages: (Long, String) -> Unit,
    viewModel: CivitAiDetailViewModel = koinViewModel(),
) {
    val connectionRepository = koinInject<NetworkConnectionRepository>()
    val shouldShowMedia by remember { derivedStateOf { connectionRepository.shouldShowMedia } }
    val dao = koinInject<FavoritesDao>()
    val dataStore = koinInject<DataStore>()
    val showBlur by dataStore.rememberShowBlur()
    val showNsfw by dataStore.showNsfw()
    val useProgressive by dataStore.rememberUseProgressive()
    val nsfwBlurStrength by dataStore.hideNsfwStrength()
    val useToolbar by dataStore.rememberUseToolbar()

    val blacklisted by dao.getBlacklisted().collectAsStateWithLifecycle(emptyList())

    val hazeState = rememberHazeState(showBlur)
    val hazeStyle = LocalHazeStyle.current

    when (val model = viewModel.models) {
        is DetailViewState.Content -> {
            val isFavorite by dao
                .getFavoritesByType(FavoriteType.Model, model.models.id)
                .collectAsStateWithLifecycle(false)

            var showQrCode by remember { mutableStateOf(false) }

            if (showQrCode) {
                ShareViaQrCode(
                    title = model.models.name,
                    url = viewModel.modelUrl,
                    qrCodeType = QrCodeType.Model,
                    id = model.models.id.toString(),
                    username = model.models.creator?.username,
                    imageUrl = model
                        .models
                        .modelVersions
                        .firstOrNull()
                        ?.images
                        ?.firstOrNull()
                        ?.url
                        .orEmpty(),
                    onClose = { showQrCode = false }
                )
            }

            var sheetDetails by remember { mutableStateOf<ModelImage?>(null) }

            sheetDetails?.let { sheetModel ->
                ImageSheet(
                    url = sheetModel.url,
                    isNsfw = sheetModel.nsfw.canNotShow(),
                    isFavorite = dao
                        .getFavoritesImages(FavoriteType.Image, sheetModel.url)
                        .collectAsStateWithLifecycle(false)
                        .value,
                    onFavorite = { viewModel.addImageToFavorites(sheetModel) },
                    onRemoveFromFavorite = { viewModel.removeImageFromFavorites(sheetModel) },
                    onDismiss = { sheetDetails = null },
                    nsfwText = sheetModel.nsfw.name,
                    moreInfo = {
                        sheetModel.meta?.let { meta ->
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

            var toolBarExpanded by remember { mutableStateOf(true) }

            var showFullDescription by remember { mutableStateOf(false) }

            Scaffold(
                topBar = {
                    TopAppBar(
                        title = {
                            Text(
                                model.models.name,
                                modifier = Modifier.basicMarquee()
                            )
                        },
                        subtitle = {
                            val models = model.models
                            Text("${models.type} by ${model.models.creator?.username}")
                        },
                        navigationIcon = { BackButton() },
                        actions = {
                            val uriHandler = LocalUriHandler.current
                            IconButton(
                                onClick = { uriHandler.openUri(viewModel.modelUrl) }
                            ) { Icon(Icons.Default.OpenInBrowser, null) }

                            AnimatedVisibility(model.models.creator != null) {
                                model.models.creator?.let { creator ->
                                    IconButton(
                                        onClick = { onNavigateToUser(creator.username.orEmpty()) },
                                    ) {
                                        LoadingImage(
                                            creator.image.orEmpty(),
                                            contentScale = ContentScale.FillBounds,
                                            modifier = Modifier
                                                .size(48.dp)
                                                .clip(CircleShape)
                                        )
                                    }
                                }
                            }
                        },
                        colors = if (showBlur)
                            TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
                        else
                            TopAppBarDefaults.topAppBarColors(),
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
                bottomBar = {
                    if (!useToolbar) {
                        BottomBarContent(
                            id = id,
                            isFavorite = isFavorite,
                            addToFavorites = viewModel::addToFavorites,
                            removeFromFavorites = viewModel::removeFromFavorites,
                            showBlur = showBlur,
                            hazeState = hazeState,
                            hazeStyle = hazeStyle,
                            onNavigateToDetailImages = onNavigateToDetailImages,
                            onShowQrCode = { showQrCode = true },
                            model = model,
                            useProgressive = useProgressive
                        )
                    }
                },
                floatingActionButton = {
                    if (useToolbar) {
                        HorizontalToolbarContent(
                            id = id,
                            isFavorite = isFavorite,
                            addToFavorites = viewModel::addToFavorites,
                            removeFromFavorites = viewModel::removeFromFavorites,
                            onNavigateToDetailImages = onNavigateToDetailImages,
                            onShowQrCode = { showQrCode = true },
                            model = model,
                            toolBarExpanded = toolBarExpanded,
                            onToggleToolbar = { toolBarExpanded = it }
                        )
                    }
                },
                floatingActionButtonPosition = FabPosition.End,
                modifier = Modifier.floatingToolbarVerticalNestedScroll(
                    expanded = toolBarExpanded,
                    onExpand = { toolBarExpanded = true },
                    onCollapse = { toolBarExpanded = false }
                )
            ) { paddingValues ->
                LazyVerticalGrid(
                    columns = adaptiveGridCell(),
                    contentPadding = paddingValues,
                    verticalArrangement = Arrangement.spacedBy(4.dp),
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    modifier = Modifier
                        .hazeSource(state = hazeState)
                        .fillMaxSize()
                ) {
                    item(
                        span = { GridItemSpan(maxLineSpan) },
                        contentType = "header"
                    ) {
                        ListItem(
                            overlineContent = { Text(model.models.type.name) },
                            headlineContent = { Text(model.models.name) },
                            trailingContent = {
                                if (model.models.nsfw) {
                                    ElevatedAssistChip(
                                        label = { Text("NSFW") },
                                        onClick = {},
                                        colors = AssistChipDefaults.elevatedAssistChipColors(
                                            disabledLabelColor = MaterialTheme.colorScheme.error,
                                            disabledContainerColor = MaterialTheme.colorScheme.surface
                                        ),
                                        border = BorderStroke(
                                            1.dp,
                                            MaterialTheme.colorScheme.error,
                                        ),
                                        enabled = false,
                                    )
                                }
                            },
                            modifier = Modifier.animateContentSize()
                        )
                    }

                    item(
                        span = { GridItemSpan(maxLineSpan) },
                        contentType = "description"
                    ) {
                        Text(
                            model.models.parsedDescription(),
                            maxLines = if (showFullDescription) Int.MAX_VALUE else 3,
                            modifier = Modifier
                                .animateContentSize()
                                .toggleable(
                                    value = showFullDescription,
                                    onValueChange = { showFullDescription = it }
                                )
                                .padding(16.dp)
                        )
                    }

                    model.models.modelVersions.forEach { version ->
                        item(
                            span = { GridItemSpan(maxLineSpan) },
                            contentType = "version"
                        ) {
                            Card(
                                onClick = { viewModel.toggleShowMoreInfo(version.id) }
                            ) {
                                TopAppBar(
                                    title = { Text("Version: ${version.name}") },
                                    navigationIcon = {
                                        version.downloadUrl?.let { downloadUrl ->
                                            val clipboard = LocalClipboardManager.current
                                            IconButton(
                                                onClick = {
                                                    clipboard.setText(AnnotatedString(downloadUrl))
                                                }
                                            ) { Icon(Icons.Default.ContentCopy, null) }
                                        }
                                    },
                                    actions = {
                                        Icon(
                                            Icons.Filled.ArrowDropDown,
                                            null,
                                            Modifier.rotate(
                                                animateFloatAsState(
                                                    if (viewModel.showMoreInfo[version.id] == true)
                                                        180f
                                                    else
                                                        0f
                                                ).value
                                            )
                                        )
                                    },
                                    windowInsets = WindowInsets(0.dp)
                                )
                                AnimatedVisibility(
                                    viewModel.showMoreInfo[version.id] == true
                                ) {
                                    version
                                        .parsedDescription()
                                        ?.let { Text(it, modifier = Modifier.padding(16.dp)) }
                                }
                            }
                        }

                        items(
                            version.images,
                            contentType = { "image" },
                            key = { it.url + it.id }
                        ) { images ->
                            AnimatedVisibility(
                                viewModel.showMoreInfo[version.id] == true,
                                enter = fadeIn() + expandVertically(),
                                exit = fadeOut() + shrinkVertically()
                            ) {
                                var showDialog by remember { mutableStateOf(false) }

                                BlacklistHandling(
                                    blacklisted = blacklisted,
                                    modelId = images.id?.toLongOrNull() ?: 0L,
                                    name = images.url,
                                    nsfw = images.nsfw.canNotShow(),
                                    imageUrl = images.url,
                                    showDialog = showDialog,
                                    onDialogDismiss = { showDialog = false }
                                )

                                val isBlacklisted by dao
                                    .getBlacklistedByImageUrl(images.url)
                                    .collectAsStateWithLifecycle(false)

                                ContextMenu(
                                    isBlacklisted = isBlacklisted,
                                    blacklistItems = blacklisted,
                                    modelId = images.id?.toLongOrNull() ?: 0L,
                                    name = images.url,
                                    nsfw = images.nsfw.canNotShow(),
                                    imageUrl = images.url,
                                ) {
                                    ImageCard(
                                        images = images,
                                        showNsfw = showNsfw,
                                        nsfwBlurStrength = nsfwBlurStrength,
                                        isFavorite = dao
                                            .getFavoritesImages(FavoriteType.Image, images.url)
                                            .collectAsStateWithLifecycle(false)
                                            .value,
                                        isBlacklisted = isBlacklisted,
                                        shouldShowMedia = shouldShowMedia,
                                        onClick = { sheetDetails = images },
                                        onLongClick = { showDialog = true }
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        is DetailViewState.Error -> {
            Scaffold(
                topBar = {
                    CenterAlignedTopAppBar(
                        title = { Text("Error") },
                        navigationIcon = { BackButton() }
                    )
                }
            ) { padding ->
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding)
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            Icons.Default.Error,
                            null,
                            modifier = Modifier.size(96.dp)
                        )
                        Text("Something went wrong")
                        Button(
                            onClick = viewModel::loadData
                        ) { Text("Try Again") }
                        model.error.message?.let { Text(it) }
                    }
                }
            }
        }

        DetailViewState.Loading -> {
            Surface {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier.fillMaxSize()
                ) {
                    CircularWavyProgressIndicator()
                }
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun ImageCard(
    images: ModelImage,
    showNsfw: Boolean,
    isFavorite: Boolean,
    isBlacklisted: Boolean,
    nsfwBlurStrength: Float,
    shouldShowMedia: Boolean,
    onClick: () -> Unit,
    onLongClick: () -> Unit,
) {
    Surface(
        tonalElevation = 4.dp,
        shape = MaterialTheme.shapes.medium,
        border = when {
            isFavorite -> BorderStroke(1.dp, MaterialTheme.colorScheme.primary)
            images.nsfw.canNotShow() -> BorderStroke(1.dp, MaterialTheme.colorScheme.error)
            else -> null
        },
        modifier = Modifier
            .size(
                width = ComposableUtils.IMAGE_WIDTH,
                height = ComposableUtils.IMAGE_HEIGHT
            )
            .clip(MaterialTheme.shapes.medium)
            .combinedClickable(
                onClick = onClick,
                onLongClick = onLongClick
            )
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
                if (images.url.endsWith("mp4") && shouldShowMedia) {
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .background(Color.Black)
                            .matchParentSize()
                    ) {
                        VideoPreviewComposable(
                            url = images.url,
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
                        imageUrl = images.url,
                        name = images.url,
                        hash = images.hash,
                        isNsfw = images.nsfw.canNotShow(),
                        modifier = Modifier.let {
                            if (!showNsfw && images.nsfw.canNotShow()) {
                                it.blur(nsfwBlurStrength.dp)
                            } else {
                                it
                            }
                        }
                    )
                }
            }

            if (images.nsfw.canNotShow()) {
                ElevatedAssistChip(
                    label = { Text("NSFW") },
                    onClick = {},
                    colors = AssistChipDefaults.elevatedAssistChipColors(
                        disabledLabelColor = MaterialTheme.colorScheme.error,
                        disabledContainerColor = MaterialTheme.colorScheme.surface
                    ),
                    enabled = false,
                    modifier = Modifier
                        .padding(horizontal = 4.dp)
                        .align(Alignment.TopEnd)
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class, ExperimentalMaterial3Api::class)
@Composable
private fun HorizontalToolbarContent(
    id: String?,
    isFavorite: Boolean,
    addToFavorites: () -> Unit,
    removeFromFavorites: () -> Unit,
    onNavigateToDetailImages: (Long, String) -> Unit,
    onShowQrCode: () -> Unit,
    model: DetailViewState.Content,
    toolBarExpanded: Boolean,
    onToggleToolbar: (Boolean) -> Unit,
) {
    val vibrantColors = FloatingToolbarDefaults.vibrantFloatingToolbarColors()

    val scope = rememberCoroutineScope()
    var showLists by remember { mutableStateOf(false) }
    val listState = rememberModalBottomSheetState(true)

    if (showLists) {
        val listDao = koinInject<ListDao>()
        val models = model.models
        ModalBottomSheet(
            onDismissRequest = { showLists = false },
            containerColor = MaterialTheme.colorScheme.surface,
            sheetState = listState
        ) {
            val toaster = koinInject<ToasterState>()
            ListChoiceScreen(
                id = models.id,
                onClick = { item ->
                    scope.launch {
                        listDao.addToList(
                            uuid = item.item.uuid,
                            id = models.id,
                            name = models.name,
                            description = models.description,
                            type = models.type.name,
                            nsfw = models.nsfw,
                            imageUrl = models.modelVersions.firstOrNull()?.images?.firstOrNull()?.url,
                            favoriteType = FavoriteType.Model,
                            hash = models.modelVersions.firstOrNull()?.images?.firstOrNull()?.hash,
                            creatorName = models.creator?.username,
                            creatorImage = models.creator?.image,
                        )
                        toaster.show("Added to ${item.item.name}", type = ToastType.Success)
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

    HorizontalFloatingToolbar(
        expanded = toolBarExpanded,
        colors = vibrantColors,
        floatingActionButton = {
            FloatingToolbarDefaults.StandardFloatingActionButton(
                onClick = { onToggleToolbar(!toolBarExpanded) }
            ) {
                Icon(
                    imageVector = Icons.Default.ArrowUpward,
                    contentDescription = null,
                    modifier = Modifier.rotate(
                        animateFloatAsState(if (toolBarExpanded) 180f else 0f).value
                    )
                )
            }
        },
        modifier = Modifier.zIndex(1f)
    ) {
        AppBarRow {
            clickableItem(
                onClick = { showLists = true },
                icon = { Icon(Icons.AutoMirrored.Filled.PlaylistAdd, null) },
                label = "Lists"
            )
            clickableItem(
                onClick = onShowQrCode,
                icon = { Icon(Icons.Default.Share, null) },
                label = "Share"
            )
            clickableItem(
                onClick = {
                    id?.toLongOrNull()
                        ?.let { onNavigateToDetailImages(it, model.models.name) }
                },
                icon = { Icon(Icons.Default.Image, null) },
                label = "Images"
            )
            clickableItem(
                onClick = {
                    if (isFavorite) {
                        removeFromFavorites()
                    } else {
                        addToFavorites()
                    }
                },
                icon = {
                    Icon(
                        if (isFavorite)
                            Icons.Default.Favorite
                        else
                            Icons.Default.FavoriteBorder,
                        null
                    )
                },
                label = if (isFavorite) "Unfavorite" else "Favorite"
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun BottomBarContent(
    id: String?,
    isFavorite: Boolean,
    removeFromFavorites: () -> Unit,
    addToFavorites: () -> Unit,
    showBlur: Boolean,
    useProgressive: Boolean,
    hazeState: HazeState,
    hazeStyle: HazeStyle,
    onNavigateToDetailImages: (Long, String) -> Unit,
    onShowQrCode: () -> Unit,
    model: DetailViewState.Content,
) {
    val scope = rememberCoroutineScope()
    var showLists by remember { mutableStateOf(false) }
    val listState = rememberModalBottomSheetState(true)

    if (showLists) {
        val listDao = koinInject<ListDao>()
        val models = model.models
        ModalBottomSheet(
            onDismissRequest = { showLists = false },
            containerColor = MaterialTheme.colorScheme.surface,
            sheetState = listState
        ) {
            val toaster = koinInject<ToasterState>()
            ListChoiceScreen(
                id = models.id,
                onClick = { item ->
                    scope.launch {
                        listDao.addToList(
                            uuid = item.item.uuid,
                            id = models.id,
                            name = models.name,
                            description = models.description,
                            type = models.type.name,
                            nsfw = models.nsfw,
                            imageUrl = models.modelVersions.firstOrNull()?.images?.firstOrNull()?.url,
                            favoriteType = FavoriteType.Model,
                            hash = models.modelVersions.firstOrNull()?.images?.firstOrNull()?.hash,
                            creatorName = models.creator?.username,
                            creatorImage = models.creator?.image,
                        )
                        toaster.show("Added to ${item.item.name}", type = ToastType.Success)
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

    BottomAppBar(
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    if (isFavorite) {
                        removeFromFavorites()
                    } else {
                        addToFavorites()
                    }
                }
            ) {
                Icon(
                    if (isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                    null
                )
            }
        },
        actions = {
            NavigationBarItem(
                selected = false,
                onClick = onShowQrCode,
                icon = { Icon(Icons.Default.Share, null) },
                label = { Text("Share") },
            )

            NavigationBarItem(
                selected = false,
                onClick = { showLists = true },
                icon = { Icon(Icons.AutoMirrored.Filled.PlaylistAdd, null) },
                label = { Text("List") },
            )

            NavigationBarItem(
                selected = false,
                onClick = {
                    id
                        ?.toLongOrNull()
                        ?.let { onNavigateToDetailImages(it, model.models.name) }
                },
                icon = { Icon(Icons.Default.Image, null) },
                label = { Text("Images") },
            )
        },
        containerColor = if (showBlur) Color.Transparent else BottomAppBarDefaults.containerColor,
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
}