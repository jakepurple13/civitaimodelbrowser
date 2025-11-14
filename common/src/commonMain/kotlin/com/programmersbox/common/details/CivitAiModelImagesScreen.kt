package com.programmersbox.common.details

import androidx.compose.animation.core.animateFloatAsState
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
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ElevatedAssistChip
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.BlurredEdgeTreatment
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.ColorMatrix
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.DialogProperties
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.programmersbox.common.BackButton
import com.programmersbox.common.ComposableUtils
import com.programmersbox.common.ContextMenu
import com.programmersbox.common.CustomModelImage
import com.programmersbox.common.LocalDataStore
import com.programmersbox.common.LocalDatabaseDao
import com.programmersbox.common.LocalNetwork
import com.programmersbox.common.SheetDetails
import com.programmersbox.common.adaptiveGridCell
import com.programmersbox.common.components.LoadingImage
import com.programmersbox.common.db.FavoriteModel
import com.programmersbox.common.home.BlacklistHandling
import com.programmersbox.common.ifTrue
import com.programmersbox.common.paging.collectAsLazyPagingItems
import com.programmersbox.common.paging.itemContentType
import com.programmersbox.common.paging.itemKey
import com.programmersbox.common.rememberSROState
import com.programmersbox.common.scaleRotateOffsetReset
import dev.chrisbanes.haze.HazeState
import dev.chrisbanes.haze.hazeEffect
import dev.chrisbanes.haze.hazeSource
import io.kamel.image.KamelImage
import io.kamel.image.asyncPainterResource

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun CivitAiModelImagesScreen(
    modelId: String?,
    modelName: String?,
) {
    val hazeState = remember { HazeState() }
    val database = LocalDatabaseDao.current
    val dataStore = LocalDataStore.current
    val showNsfw by remember { dataStore.showNsfw.flow }.collectAsStateWithLifecycle(false)
    val nsfwBlurStrength by remember { dataStore.hideNsfwStrength.flow }.collectAsStateWithLifecycle(6f)
    val showBlur by dataStore.rememberShowBlur()
    val network = LocalNetwork.current
    val viewModel = viewModel {
        CivitAiModelImagesViewModel(
            modelId = modelId,
            dataStore = dataStore,
            network = network,
            database = database
        )
    }
    val uriHandler = LocalUriHandler.current
    val lazyPagingItems = viewModel.pager.collectAsLazyPagingItems()

    val favoriteList by database.getFavorites().collectAsStateWithLifecycle(emptyList())
    val blacklisted by database.getBlacklisted().collectAsStateWithLifecycle(emptyList())

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
                navigationIcon = { BackButton() },
                actions = { Text("(${lazyPagingItems.itemCount})") },
                colors = if (showBlur) TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
                else TopAppBarDefaults.topAppBarColors(),
                modifier = Modifier.ifTrue(showBlur) { hazeEffect(hazeState) }
            )
        },
    ) { padding ->
        LazyVerticalGrid(
            columns = adaptiveGridCell(),
            contentPadding = padding,
            verticalArrangement = Arrangement.spacedBy(4.dp),
            horizontalArrangement = Arrangement.spacedBy(4.dp),
            modifier = Modifier
                .hazeSource(state = hazeState)
                .fillMaxSize()
        ) {
            items(
                count = lazyPagingItems.itemCount,
                contentType = lazyPagingItems.itemContentType(),
                key = lazyPagingItems.itemKey { it.url }
            ) {
                lazyPagingItems[it]?.let { models ->

                    var showDialog by remember { mutableStateOf(false) }

                    BlacklistHandling(
                        blacklisted = blacklisted,
                        modelId = models.postId ?: 0L,
                        name = models.url,
                        nsfw = models.nsfwLevel.canNotShow(),
                        imageUrl = models.url,
                        showDialog = showDialog,
                        onDialogDismiss = { showDialog = false }
                    )

                    ContextMenu(
                        isBlacklisted = blacklisted.any { it.imageUrl == models.url },
                        blacklistItems = blacklisted,
                        modelId = models.postId ?: 0L,
                        name = models.url,
                        nsfw = models.nsfwLevel.canNotShow(),
                        imageUrl = models.url,
                    ) {
                        ImageCard(
                            images = models,
                            showNsfw = showNsfw,
                            nsfwBlurStrength = nsfwBlurStrength,
                            isFavorite = favoriteList
                                .filterIsInstance<FavoriteModel.Image>()
                                .any { f -> f.imageUrl == models.url },
                            isBlacklisted = blacklisted.any { it.imageUrl == models.url },
                            onClick = {
                                if (models.height < 2000 || models.width < 2000) {
                                    sheetDetails = models
                                } else {
                                    uriHandler.openUri(models.url)
                                }
                            },
                            onLongClick = { showDialog = true }
                        )
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun ImageCard(
    images: CustomModelImage,
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
            isFavorite -> MaterialTheme.colorScheme.primary
            images.nsfwLevel.canNotShow() -> MaterialTheme.colorScheme.error
            images.height > 2000 || images.width > 2000 -> MaterialTheme.colorScheme.secondary
            else -> null
        }?.let { BorderStroke(1.dp, it) },
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
            if (images.height < 2000 || images.width < 2000) {
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
                        isNsfw = images.nsfwLevel.canNotShow(),
                        modifier = Modifier.let {
                            if (!showNsfw && images.nsfwLevel.canNotShow()) {
                                it.blur(nsfwBlurStrength.dp)
                            } else {
                                it
                            }
                        }
                    )
                }
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