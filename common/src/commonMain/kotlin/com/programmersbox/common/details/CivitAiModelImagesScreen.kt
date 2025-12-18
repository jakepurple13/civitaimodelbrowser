package com.programmersbox.common.details

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.basicMarquee
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.ElevatedAssistChip
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.paging.compose.collectAsLazyPagingItems
import androidx.paging.compose.itemContentType
import androidx.paging.compose.itemKey
import chaintech.videoplayer.ui.preview.VideoPreviewComposable
import com.programmersbox.common.BackButton
import com.programmersbox.common.ComposableUtils
import com.programmersbox.common.ContextMenu
import com.programmersbox.common.CustomModelImage
import com.programmersbox.common.DataStore
import com.programmersbox.common.NetworkConnectionRepository
import com.programmersbox.common.adaptiveGridCell
import com.programmersbox.common.components.ImageSheet
import com.programmersbox.common.components.LoadingImage
import com.programmersbox.common.db.FavoritesDao
import com.programmersbox.common.home.BlacklistHandling
import com.programmersbox.common.ifTrue
import dev.chrisbanes.haze.HazeState
import dev.chrisbanes.haze.hazeEffect
import dev.chrisbanes.haze.hazeSource
import org.koin.compose.koinInject
import org.koin.compose.viewmodel.koinViewModel

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun CivitAiModelImagesScreen(
    modelName: String?,
    viewModel: CivitAiModelImagesViewModel = koinViewModel(),
) {
    val connectionRepository = koinInject<NetworkConnectionRepository>()
    val shouldShowMedia by remember { derivedStateOf { connectionRepository.shouldShowMedia } }
    val hazeState = remember { HazeState() }
    val database = koinInject<FavoritesDao>()
    val dataStore = koinInject<DataStore>()
    val showNsfw by dataStore.showNsfw()
    val nsfwBlurStrength by dataStore.hideNsfwStrength()
    val showBlur by dataStore.rememberShowBlur()
    val uriHandler = LocalUriHandler.current
    val lazyPagingItems = viewModel.pager.collectAsLazyPagingItems()

    val favoriteList by viewModel
        .favoriteList
        .collectAsStateWithLifecycle(emptyList())
    val blacklisted by database.getBlacklisted().collectAsStateWithLifecycle(emptyList())

    var sheetDetails by remember { mutableStateOf<CustomModelImage?>(null) }

    sheetDetails?.let { sheetModel ->
        ImageSheet(
            url = sheetModel.url,
            isNsfw = sheetModel.nsfwLevel.canNotShow(),
            isFavorite = favoriteList.any { f -> f.imageUrl == sheetModel.url },
            onFavorite = { viewModel.addImageToFavorites(sheetModel) },
            onRemoveFromFavorite = { viewModel.removeImageFromFavorites(sheetModel) },
            onDismiss = { sheetDetails = null },
            nsfwText = sheetModel.nsfwLevel.name,
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
                contentType = lazyPagingItems.itemContentType { "image" },
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
                            isFavorite = favoriteList.any { f -> f.imageUrl == models.url },
                            isBlacklisted = blacklisted.any { it.imageUrl == models.url },
                            shouldShowMedia = shouldShowMedia,
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
    shouldShowMedia: Boolean,
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
                            isNsfw = images.nsfwLevel.canNotShow(),
                            hash = images.hash,
                            modifier = Modifier.let {
                                if (!showNsfw && images.nsfwLevel.canNotShow()) {
                                    it.blur(nsfwBlurStrength.dp)
                                } else {
                                    it
                                }
                            }
                        )
                    }
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

fun Modifier.blurGradient(
    blur: Dp = 70.dp,
    alpha: Float = .5f,
    scaleX: Float = 1.5f,
    scaleY: Float = 1.5f,
) = scale(scaleX, scaleY)
    .blur(blur, BlurredEdgeTreatment.Unbounded)
    .alpha(alpha)