package com.programmersbox.common.details

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
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.toggleable
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.OpenInBrowser
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.BottomAppBar
import androidx.compose.material3.BottomAppBarDefaults
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ElevatedAssistChip
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
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
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.ColorMatrix
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.DialogProperties
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.programmersbox.common.BackButton
import com.programmersbox.common.ComposableUtils
import com.programmersbox.common.ContextMenu
import com.programmersbox.common.LocalDataStore
import com.programmersbox.common.LocalDatabaseDao
import com.programmersbox.common.LocalNetwork
import com.programmersbox.common.ModelImage
import com.programmersbox.common.Network
import com.programmersbox.common.SheetDetails
import com.programmersbox.common.adaptiveGridCell
import com.programmersbox.common.components.LoadingImage
import com.programmersbox.common.db.FavoriteModel
import com.programmersbox.common.home.BlacklistHandling
import com.programmersbox.common.ifTrue
import com.programmersbox.common.rememberSROState
import com.programmersbox.common.scaleRotateOffsetReset
import dev.chrisbanes.haze.HazeProgressive
import dev.chrisbanes.haze.HazeState
import dev.chrisbanes.haze.LocalHazeStyle
import dev.chrisbanes.haze.hazeChild
import dev.chrisbanes.haze.hazeEffect
import dev.chrisbanes.haze.hazeSource
import io.kamel.image.KamelImage
import io.kamel.image.asyncPainterResource
import java.text.SimpleDateFormat
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun CivitAiDetailScreen(
    id: String?,
    onShareClick: (String) -> Unit,
    network: Network = LocalNetwork.current,
    onNavigateToUser: (String) -> Unit,
    onNavigateToDetailImages: (Long, String) -> Unit,
) {
    val scope = rememberCoroutineScope()
    val hazeState = remember { HazeState() }
    val dao = LocalDatabaseDao.current
    val viewModel = viewModel { CivitAiDetailViewModel(network, id, dao) }
    val simpleDateTimeFormatter = remember { SimpleDateFormat("MM/dd/yy HH:mm", Locale.getDefault()) }
    val dataStore = LocalDataStore.current
    val showBlur by dataStore.rememberShowBlur()
    val showNsfw by remember { dataStore.showNsfw.flow }.collectAsStateWithLifecycle(false)
    val nsfwBlurStrength by remember { dataStore.hideNsfwStrength.flow }.collectAsStateWithLifecycle(6f)

    val favoriteList by dao.getFavoriteModels().collectAsStateWithLifecycle(emptyList())
    val blacklisted by dao.getBlacklisted().collectAsStateWithLifecycle(emptyList())

    val hazeStyle = LocalHazeStyle.current

    when (val model = viewModel.models) {
        is DetailViewState.Content -> {
            var sheetDetails by remember { mutableStateOf<ModelImage?>(null) }

            sheetDetails?.let { sheetModel ->
                SheetDetails(
                    onDismiss = { sheetDetails = null },
                    content = {
                        SheetContent(
                            image = sheetModel,
                            isFavorite = favoriteList
                                .filterIsInstance<FavoriteModel.Image>()
                                .any { f -> f.imageUrl == sheetModel.url },
                            onFavorite = { viewModel.addImageToFavorites(sheetModel) },
                            onRemoveFromFavorite = { viewModel.removeImageFromFavorites(sheetModel) }
                        )
                    }
                )
            }

            Scaffold(
                topBar = {
                    TopAppBar(
                        title = {
                            Text(
                                model.models.name,
                                modifier = Modifier.basicMarquee()
                            )
                        },
                        navigationIcon = { BackButton() },
                        actions = {
                            model.models.creator?.let { creator ->
                                IconButton(
                                    onClick = { onNavigateToUser(creator.username.orEmpty()) },
                                ) {
                                    LoadingImage(
                                        creator.image.orEmpty(),
                                        modifier = Modifier
                                            .size(48.dp)
                                            .clip(CircleShape)
                                    )
                                }
                            }
                        },
                        colors = if (showBlur) TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
                        else TopAppBarDefaults.topAppBarColors(),
                        modifier = Modifier.ifTrue(showBlur) {
                            hazeEffect(hazeState, hazeStyle) {
                                progressive = HazeProgressive.verticalGradient(
                                    startIntensity = 1f,
                                    endIntensity = 0f,
                                    preferPerformance = true
                                )
                            }
                        }
                    )
                },
                bottomBar = {
                    BottomAppBar(
                        floatingActionButton = {
                            FloatingActionButton(
                                onClick = {
                                    if (viewModel.isFavorite) {
                                        viewModel.removeFromFavorites()
                                    } else {
                                        viewModel.addToFavorites()
                                    }
                                }
                            ) {
                                Icon(
                                    if (viewModel.isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                                    null
                                )
                            }
                        },
                        actions = {
                            NavigationBarItem(
                                selected = false,
                                onClick = { onShareClick(viewModel.modelUrl) },
                                icon = { Icon(Icons.Default.Share, null) },
                                label = { Text("Share") },
                            )

                            val uriHandler = LocalUriHandler.current
                            NavigationBarItem(
                                selected = false,
                                onClick = { uriHandler.openUri(viewModel.modelUrl) },
                                icon = { Icon(Icons.Default.OpenInBrowser, null) },
                                label = { Text("Browser") },
                            )

                            NavigationBarItem(
                                selected = false,
                                onClick = {
                                    id?.toLongOrNull()
                                        ?.let { onNavigateToDetailImages(it, model.models.name) }
                                },
                                icon = { Icon(Icons.Default.Image, null) },
                                label = { Text("Images") },
                            )
                        },
                        containerColor = if (showBlur) Color.Transparent else BottomAppBarDefaults.containerColor,
                        modifier = Modifier.ifTrue(showBlur) {
                            hazeChild(hazeState, hazeStyle) {
                                //TODO: Fix this
                                progressive = HazeProgressive.verticalGradient(
                                    startIntensity = 0f,
                                    endIntensity = 1f,
                                    preferPerformance = true
                                )
                            }
                        }
                    )
                },
            ) { paddingValues ->
                LazyVerticalGrid(
                    columns = adaptiveGridCell(),
                    contentPadding = paddingValues,
                    verticalArrangement = Arrangement.spacedBy(4.dp),
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    modifier = Modifier
                        .ifTrue(showBlur) { hazeSource(state = hazeState) }
                        .fillMaxSize()
                ) {
                    item(
                        span = { GridItemSpan(maxLineSpan) }
                    ) {
                        ListItem(
                            overlineContent = model.models.creator?.username?.let { { Text("Made by $it") } },
                            leadingContent = { Text(model.models.type.name) },
                            headlineContent = { Text(model.models.name) },
                            supportingContent = {
                                var showFullDescription by remember { mutableStateOf(false) }
                                Text(
                                    model.models.parsedDescription(),
                                    maxLines = if (showFullDescription) Int.MAX_VALUE else 3,
                                    modifier = Modifier
                                        .animateContentSize()
                                        .toggleable(
                                            value = showFullDescription,
                                            onValueChange = { showFullDescription = it }
                                        )
                                )
                            },
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

                    model.models.modelVersions.forEachIndexed { index, version ->

                        var showImages by mutableStateOf(index == 0)
                        var showMoreInfo by mutableStateOf(index == 0)

                        item(
                            span = { GridItemSpan(maxLineSpan) }
                        ) {
                            Card(
                                onClick = {
                                    showImages = !showImages
                                    showMoreInfo = !showMoreInfo
                                }
                            ) {
                                TopAppBar(
                                    title = { Text("Version: ${version.name}") },
                                    navigationIcon = {
                                        version.downloadUrl?.let { downloadUrl ->
                                            val clipboard = LocalClipboardManager.current
                                            IconButton(
                                                onClick = { clipboard.setText(AnnotatedString(downloadUrl)) }
                                            ) { Icon(Icons.Default.ContentCopy, null) }
                                        }
                                    },
                                    actions = {
                                        Icon(
                                            Icons.Filled.ArrowDropDown,
                                            null,
                                            Modifier.rotate(if (showMoreInfo) 180f else 0f)
                                        )
                                    },
                                    windowInsets = WindowInsets(0.dp)
                                )
                                AnimatedVisibility(showMoreInfo) {
                                    ListItem(
                                        headlineContent = {
                                            //Text("Last Update at: " + simpleDateTimeFormatter.format(version.createdAt.toEpochMilliseconds()))
                                        },
                                        supportingContent = version.parsedDescription()?.let { { Text(it) } }
                                    )
                                }
                            }
                        }

                        items(version.images) { images ->
                            AnimatedVisibility(
                                showImages,
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
                                ContextMenu(
                                    isBlacklisted = blacklisted.any { it.imageUrl == images.url },
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
                                        isFavorite = favoriteList
                                            .filterIsInstance<FavoriteModel.Image>()
                                            .any { f -> f.imageUrl == images.url },
                                        isBlacklisted = blacklisted.any { it.imageUrl == images.url },
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
            Surface {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier.fillMaxSize()
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
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
                    CircularProgressIndicator()
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
                LoadingImage(
                    imageUrl = images.url,
                    name = images.url,
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

@OptIn(ExperimentalFoundationApi::class, ExperimentalMaterial3Api::class)
@Composable
private fun SheetContent(
    image: ModelImage,
    isFavorite: Boolean,
    onFavorite: () -> Unit,
    onRemoveFromFavorite: () -> Unit,
) {
    val painter = asyncPainterResource(image.url)
    SelectionContainer(
        modifier = Modifier.navigationBarsPadding()
    ) {
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
                        resource = { painter },
                        onLoading = {
                            val progress = animateFloatAsState(
                                targetValue = it,
                                label = ""
                            ).value
                            CircularProgressIndicator(
                                progress = { progress }
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
                windowInsets = WindowInsets(0.dp),
                actions = {
                    IconButton(
                        onClick = {
                            if (isFavorite) {
                                onRemoveFromFavorite()
                            } else {
                                onFavorite()
                            }
                        }
                    ) {
                        Icon(
                            if (isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                            null
                        )
                    }
                }
            )

            val blur = 70.dp
            val alpha = .5f
            val saturation = 3f
            val scaleX = 1.5f
            val scaleY = 1.5f

            Box(modifier = Modifier.align(Alignment.CenterHorizontally)) {
                KamelImage(
                    resource = { painter },
                    contentDescription = null,
                    colorFilter = ColorFilter.colorMatrix(ColorMatrix().apply { setToSaturation(saturation) }),
                    modifier = Modifier.blurGradient(blur, alpha, scaleX, scaleY)
                )

                KamelImage(
                    resource = { painter },
                    contentDescription = null,
                    onLoading = {
                        val progress = animateFloatAsState(
                            targetValue = it,
                            label = ""
                        ).value
                        CircularProgressIndicator(
                            progress = { progress }
                        )
                    },
                    modifier = Modifier.combinedClickable(onDoubleClick = { imagePopup = true }) {}
                )
            }

            if (image.nsfw.canNotShow()) {
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

                    ElevatedAssistChip(
                        label = { Text(image.nsfw.name) },
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
            image.meta?.let { meta ->
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