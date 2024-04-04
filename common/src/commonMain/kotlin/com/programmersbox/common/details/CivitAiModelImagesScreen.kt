@file:Suppress("INLINE_FROM_HIGHER_PLATFORM")

package com.programmersbox.common.details

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.BlurredEdgeTreatment
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.ColorMatrix
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.DialogProperties
import androidx.lifecycle.createSavedStateHandle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.programmersbox.common.*
import com.programmersbox.common.components.LoadingImage
import com.programmersbox.common.db.FavoriteModel
import com.programmersbox.common.paging.collectAsLazyPagingItems
import com.programmersbox.common.paging.itemContentType
import com.programmersbox.common.paging.itemKey
import dev.chrisbanes.haze.HazeState
import dev.chrisbanes.haze.haze
import dev.chrisbanes.haze.hazeChild
import io.kamel.image.KamelImage
import io.kamel.image.asyncPainterResource
import moe.tlaster.precompose.flow.collectAsStateWithLifecycle

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun CivitAiModelImagesScreen(
    modelId: String?,
    modelName: String?,
) {
    val hazeState = remember { HazeState() }
    val database = LocalDatabase.current
    val dataStore = LocalDataStore.current
    val showNsfw by remember { dataStore.showNsfw.flow }.collectAsStateWithLifecycle(false)
    val nsfwBlurStrength by remember { dataStore.hideNsfwStrength.flow }.collectAsStateWithLifecycle(6f)
    val network = LocalNetwork.current
    val viewModel = viewModel {
        CivitAiModelImagesViewModel(
            //modelId = modelId,
            dataStore = dataStore,
            network = network,
            database = database,
            createSavedStateHandle()
        )
    }
    val uriHandler = LocalUriHandler.current
    val lazyPagingItems = viewModel.pager.collectAsLazyPagingItems()
    val navController = LocalNavController.current

    val favoriteList by database.getFavorites().collectAsStateWithLifecycle(emptyList())

    var sheetDetails by remember { mutableStateOf<CustomModelImage?>(null) }

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
                        modelName.orEmpty(),
                        modifier = Modifier.basicMarquee()
                    )
                },
                navigationIcon = {
                    IconButton(
                        onClick = { navController.popBackStack() }
                    ) { Icon(Icons.Default.ArrowBack, null) }
                },
                actions = { Text("(${lazyPagingItems.itemCount})") },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent),
                modifier = Modifier.hazeChild(hazeState)
            )
        },
    ) { padding ->
        LazyVerticalGrid(
            columns = adaptiveGridCell(),
            contentPadding = padding,
            verticalArrangement = Arrangement.spacedBy(4.dp),
            horizontalArrangement = Arrangement.spacedBy(4.dp),
            modifier = Modifier
                .haze(
                    state = hazeState,
                    backgroundColor = MaterialTheme.colorScheme.surface
                )
                .fillMaxSize()
        ) {
            items(
                count = lazyPagingItems.itemCount,
                contentType = lazyPagingItems.itemContentType(),
                key = lazyPagingItems.itemKey { it.url }
            ) {
                lazyPagingItems[it]?.let { models ->
                    ImageCard(
                        images = models,
                        showNsfw = showNsfw,
                        nsfwBlurStrength = nsfwBlurStrength,
                        isFavorite = favoriteList
                            .filterIsInstance<FavoriteModel.Image>()
                            .any { f -> f.imageUrl == models.url },
                        onClick = {
                            if (models.height < 2000 || models.width < 2000) {
                                sheetDetails = models
                            } else {
                                uriHandler.openUri(models.url)
                            }
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun ImageCard(
    images: CustomModelImage,
    showNsfw: Boolean,
    isFavorite: Boolean,
    nsfwBlurStrength: Float,
    onClick: () -> Unit,
) {
    Surface(
        tonalElevation = 4.dp,
        shape = MaterialTheme.shapes.medium,
        border = when {
            isFavorite -> MaterialTheme.colorScheme.primary
            images.nsfwLevel.canNotShow() -> MaterialTheme.colorScheme.error
            images.height > 2000 || images.width > 2000 -> MaterialTheme.colorScheme.secondary
            else -> null
        }?.let { BorderStroke(1.dp, it) },
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
            if (images.height < 2000 || images.width < 2000) {
                LoadingImage(
                    imageUrl = images.url,
                    name = images.url,
                    isNsfw = images.nsfwLevel.canNotShow(),
                    modifier = Modifier.let {
                        if (!showNsfw && images.nsfwLevel.canNotShow()) {
                            it.blur(nsfwBlurStrength.dp)
                        } else {
                            it
                        }
                    }
                )
            } else {
                Text("Too Large")
            }

            if (images.nsfwLevel.canNotShow()) {
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
    image: CustomModelImage,
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
                        resource = painter,
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
                title = { Text("By ${image.username}") },
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
                    resource = painter,
                    contentDescription = null,
                    colorFilter = ColorFilter.colorMatrix(ColorMatrix().apply { setToSaturation(saturation) }),
                    modifier = Modifier.blurGradient(blur, alpha, scaleX, scaleY)
                )

                KamelImage(
                    resource = painter,
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

            if (image.nsfwLevel.canNotShow()) {
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
                        label = { Text(image.nsfwLevel.name) },
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

fun Modifier.blurGradient(
    blur: Dp = 70.dp,
    alpha: Float = .5f,
    scaleX: Float = 1.5f,
    scaleY: Float = 1.5f,
) = scale(scaleX, scaleY)
    .blur(blur, BlurredEdgeTreatment.Unbounded)
    .alpha(alpha)