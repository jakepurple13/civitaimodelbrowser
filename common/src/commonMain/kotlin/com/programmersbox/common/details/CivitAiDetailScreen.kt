@file:Suppress("INLINE_FROM_HIGHER_PLATFORM")

package com.programmersbox.common.details

import androidx.compose.animation.*
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.selection.toggleable
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.DialogProperties
import com.programmersbox.common.*
import com.programmersbox.common.components.LoadingImage
import com.programmersbox.common.db.FavoriteModel
import dev.chrisbanes.haze.HazeState
import dev.chrisbanes.haze.haze
import dev.chrisbanes.haze.hazeChild
import io.kamel.image.KamelImage
import io.kamel.image.asyncPainterResource
import moe.tlaster.precompose.flow.collectAsStateWithLifecycle
import moe.tlaster.precompose.viewmodel.viewModel
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun CivitAiDetailScreen(
    id: String?,
    onShareClick: (String) -> Unit,
    network: Network = LocalNetwork.current,
) {
    val hazeState = remember { HazeState() }
    val database = LocalDatabase.current
    val viewModel = viewModel { CivitAiDetailViewModel(network, id, database) }
    val navController = LocalNavController.current
    val simpleDateTimeFormatter = remember { SimpleDateFormat("MM/dd/yy HH:mm", Locale.getDefault()) }
    val dataStore = LocalDataStore.current
    val showNsfw by remember { dataStore.showNsfw.flow }.collectAsStateWithLifecycle(false)
    val nsfwBlurStrength by remember { dataStore.hideNsfwStrength.flow }.collectAsStateWithLifecycle(6f)

    val favoriteList by database.getFavorites().collectAsStateWithLifecycle(emptyList())

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
                        navigationIcon = {
                            IconButton(
                                onClick = { navController.popBackStack() }
                            ) { Icon(Icons.Default.ArrowBack, null) }
                        },
                        actions = {
                            model.models.creator?.let { creator ->
                                IconButton(
                                    onClick = { navController.navigateToUser(creator.username.orEmpty()) },
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
                        colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent),
                        modifier = Modifier.hazeChild(hazeState)
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
                                        ?.let { navController.navigateToDetailImages(it, model.models.name) }
                                },
                                icon = { Icon(Icons.Default.Image, null) },
                                label = { Text("Images") },
                            )
                        },
                        containerColor = Color.Transparent,
                        modifier = Modifier.hazeChild(hazeState)
                    )
                },
            ) { paddingValues ->
                LazyVerticalGrid(
                    columns = adaptiveGridCell(),
                    contentPadding = paddingValues,
                    verticalArrangement = Arrangement.spacedBy(4.dp),
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    modifier = Modifier
                        .haze(
                            state = hazeState,
                            backgroundColor = MaterialTheme.colorScheme.surface
                        )
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
                                ImageCard(
                                    images = images,
                                    showNsfw = showNsfw,
                                    nsfwBlurStrength = nsfwBlurStrength,
                                    isFavorite = favoriteList
                                        .filterIsInstance<FavoriteModel.Image>()
                                        .any { f -> f.imageUrl == images.url },
                                    onClick = { sheetDetails = images }
                                )
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

@Composable
private fun ImageCard(
    images: ModelImage,
    showNsfw: Boolean,
    isFavorite: Boolean,
    nsfwBlurStrength: Float,
    onClick: () -> Unit,
) {
    Surface(
        tonalElevation = 4.dp,
        shape = MaterialTheme.shapes.medium,
        border = when {
            isFavorite -> BorderStroke(1.dp, MaterialTheme.colorScheme.primary)
            images.nsfw.canNotShow() -> BorderStroke(1.dp, MaterialTheme.colorScheme.error)
            else -> null
        },
        onClick = onClick,
        modifier = Modifier.size(
            width = ComposableUtils.IMAGE_WIDTH,
            height = ComposableUtils.IMAGE_HEIGHT
        )
    ) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
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