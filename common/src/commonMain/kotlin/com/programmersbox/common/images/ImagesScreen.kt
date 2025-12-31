package com.programmersbox.common.images

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.sizeIn
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.shape.CircleShape
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
import androidx.compose.material3.TextButton
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
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.paging.compose.collectAsLazyPagingItems
import androidx.paging.compose.itemContentType
import chaintech.videoplayer.ui.preview.VideoPreviewComposable
import com.programmersbox.common.BackButton
import com.programmersbox.common.ComposableUtils
import com.programmersbox.common.CustomModelImage
import com.programmersbox.common.DataStore
import com.programmersbox.common.NetworkConnectionRepository
import com.programmersbox.common.adaptiveGridCell
import com.programmersbox.common.components.ImageSheet
import com.programmersbox.common.components.LoadingImage
import com.programmersbox.common.components.MultipleImageSheet
import com.programmersbox.common.db.FavoritesDao
import com.programmersbox.common.home.BlacklistHandling
import com.programmersbox.common.ifTrue
import com.programmersbox.common.paging.itemKeyIndexed
import dev.chrisbanes.haze.HazeState
import dev.chrisbanes.haze.hazeEffect
import dev.chrisbanes.haze.hazeSource
import org.koin.compose.koinInject
import org.koin.compose.viewmodel.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CivitAiImagesScreen(
    viewModel: CivitAiImagesViewModel = koinViewModel(),
    onNavigateToUser: (String) -> Unit,
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
    val lazyPagingItems = viewModel
        .pager
        .collectAsLazyPagingItems()

    val favoriteList by viewModel
        .favoriteList
        .collectAsStateWithLifecycle(emptyList())

    val blacklisted by database
        .getBlacklisted()
        .collectAsStateWithLifecycle(emptyList())

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
            actions = {
                sheetModel.username?.let { creator ->
                    TextButton(
                        onClick = { onNavigateToUser(creator) },
                    ) { Text(creator) }
                }
            },
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

    var sheetDetailsMultiple by remember { mutableStateOf<List<CustomModelImage>?>(null) }

    sheetDetailsMultiple?.let { sheetModel ->
        MultipleImageSheet(
            urls = sheetModel.map { it.url },
            onDismiss = { sheetDetailsMultiple = null },
            actions = {
                sheetModel.firstOrNull()?.username?.let { creator ->
                    TextButton(
                        onClick = { onNavigateToUser(creator) },
                    ) { Text(creator) }
                }
            },
            moreInfo = { }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Images") },
                navigationIcon = { BackButton() },
                actions = { Text("(${lazyPagingItems.itemCount})") },
                colors = if (showBlur)
                    TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
                else
                    TopAppBarDefaults.topAppBarColors(),
                modifier = Modifier.ifTrue(showBlur) { hazeEffect(hazeState) }
            )
        },
    ) { padding ->
        LazyVerticalGrid(
            columns = adaptiveGridCell(
                maxCount = 5
            ),
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
                //key = lazyPagingItems.itemKey { it.url }
                key = lazyPagingItems.itemKeyIndexed { item, index -> "$item$index" }
            ) {
                lazyPagingItems[it]?.let { models ->
                    if (models.second.size > 1) {
                        ImageCard2(
                            images = models.second,
                            showNsfw = showNsfw,
                            nsfwBlurStrength = nsfwBlurStrength,
                            onClick = { sheetDetailsMultiple = models.second },
                            shouldShowMedia = shouldShowMedia,
                            onLongClick = {}
                        )
                    } else {
                        val modelInfo = models.second.first()
                        var showDialog by remember { mutableStateOf(false) }

                        BlacklistHandling(
                            blacklisted = blacklisted,
                            modelId = modelInfo.postId ?: 0L,
                            name = modelInfo.url,
                            nsfw = modelInfo.nsfwLevel.canNotShow(),
                            imageUrl = modelInfo.url,
                            showDialog = showDialog,
                            onDialogDismiss = { showDialog = false }
                        )

                        ImageCard(
                            images = modelInfo,
                            showNsfw = showNsfw,
                            nsfwBlurStrength = nsfwBlurStrength,
                            isFavorite = favoriteList.any { f -> f.imageUrl == modelInfo.url },
                            isBlacklisted = blacklisted.any { it.imageUrl == modelInfo.url },
                            shouldShowMedia = shouldShowMedia,
                            onClick = {
                                if (modelInfo.height < 2000 || modelInfo.width < 2000) {
                                    sheetDetails = modelInfo
                                } else {
                                    uriHandler.openUri(modelInfo.url)
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
            .sizeIn(
                minHeight = 100.dp,
                maxHeight = ComposableUtils.IMAGE_HEIGHT / 2,
                maxWidth = ComposableUtils.IMAGE_WIDTH
            )
            .clip(MaterialTheme.shapes.medium)
            .combinedClickable(
                onClick = onClick,
                onLongClick = onLongClick
            )
            .animateContentSize()
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier.fillMaxSize()
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

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun ImageCard2(
    images: List<CustomModelImage>,
    showNsfw: Boolean,
    nsfwBlurStrength: Float,
    shouldShowMedia: Boolean,
    onClick: () -> Unit,
    onLongClick: () -> Unit,
) {
    Surface(
        tonalElevation = 4.dp,
        shape = MaterialTheme.shapes.medium,
        modifier = Modifier
            .sizeIn(
                minHeight = 100.dp,
                maxHeight = ComposableUtils.IMAGE_HEIGHT / 2,
                maxWidth = ComposableUtils.IMAGE_WIDTH,
            )
            .clip(MaterialTheme.shapes.medium)
            .combinedClickable(
                onClick = onClick,
                onLongClick = onLongClick
            )
            .animateContentSize()
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier.fillMaxSize()
        ) {
            Column(
                modifier = Modifier.matchParentSize()
            ) {
                images.chunked(3).forEach { chunk ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f, true)
                    ) {
                        chunk.forEach { image ->
                            if (image.url.endsWith("mp4") && shouldShowMedia) {
                                Box(
                                    contentAlignment = Alignment.Center,
                                    modifier = Modifier
                                        .background(Color.Black)
                                        .weight(1f)
                                ) {
                                    Text(
                                        "Video",
                                        modifier = Modifier.padding(4.dp)
                                    )
                                }
                            } else {
                                LoadingImage(
                                    imageUrl = image.url,
                                    name = image.url,
                                    isNsfw = image.nsfwLevel.canNotShow(),
                                    hash = image.hash,
                                    modifier = Modifier
                                        .let {
                                            if (!showNsfw && image.nsfwLevel.canNotShow()) {
                                                it.blur(nsfwBlurStrength.dp)
                                            } else {
                                                it
                                            }
                                        }
                                        .weight(1f)
                                        .fillMaxWidth()
                                )
                            }
                        }
                    }
                }
            }

            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .size(48.dp)
                    .background(Color.Black.copy(alpha = .5f), CircleShape)
            ) { Text(images.size.toString()) }

            if (images.any { it.nsfwLevel.canNotShow() }) {
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