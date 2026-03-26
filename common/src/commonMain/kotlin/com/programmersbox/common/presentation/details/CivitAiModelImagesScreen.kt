package com.programmersbox.common.presentation.details

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.basicMarquee
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.staggeredgrid.LazyVerticalStaggeredGrid
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridCells
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridItemSpan
import androidx.compose.foundation.lazy.staggeredgrid.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.CircularWavyProgressIndicator
import androidx.compose.material3.ElevatedAssistChip
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.programmersbox.common.BackButton
import com.programmersbox.common.ContextMenu
import com.programmersbox.common.CustomModelImage
import com.programmersbox.common.DataStore
import com.programmersbox.common.db.FavoritesDao
import com.programmersbox.common.presentation.components.ImageSheet
import com.programmersbox.common.presentation.components.LoadingImage
import com.programmersbox.common.presentation.components.blurkind.rememberBlurKindState
import com.programmersbox.common.presentation.components.blurkind.setBlurKind
import com.programmersbox.common.presentation.components.blurkind.setBlurKindSource
import com.programmersbox.common.presentation.components.videoloader.VideoThumbnailLoader
import com.programmersbox.common.presentation.home.BlacklistHandling
import dev.chrisbanes.haze.HazeProgressive
import org.koin.compose.koinInject
import org.koin.compose.viewmodel.koinViewModel

@OptIn(
    ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class,
    ExperimentalMaterial3ExpressiveApi::class
)
@Composable
fun CivitAiModelImagesScreen(
    modelName: String?,
    viewModel: CivitAiModelImagesViewModel = koinViewModel(),
) {
    val database = koinInject<FavoritesDao>()
    val dataStore = koinInject<DataStore>()
    val showNsfw by dataStore.showNsfw()
    val nsfwBlurStrength by dataStore.hideNsfwStrength()
    val uriHandler = LocalUriHandler.current

    val blurKindState = rememberBlurKindState()

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
                //actions = { Text("(${lazyPagingItems.itemCount})") },
                actions = { Text("(${viewModel.imagesList.size})") },
                colors = if (blurKindState.showBlur) TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.Transparent
                )
                else TopAppBarDefaults.topAppBarColors(),
                modifier = Modifier.setBlurKind(blurKindState) {
                    progressive = if (blurKindState.hazeState.useProgressive)
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
    ) { padding ->
        LazyVerticalStaggeredGrid(
            columns = StaggeredGridCells.Fixed(2),
            contentPadding = padding,
            verticalItemSpacing = 4.dp,
            horizontalArrangement = Arrangement.spacedBy(4.dp),
            modifier = Modifier
                .setBlurKindSource(blurKindState)
                .fillMaxSize()
        ) {
            when (val state = viewModel.uiState) {
                is ImageUiState.Error -> {
                    item(
                        key = "error",
                        span = StaggeredGridItemSpan.FullLine,
                        contentType = "error"
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                        ) {
                            Text("Error loading images")
                            Text(state.throwable.message ?: "Unknown error")
                        }
                    }
                }

                ImageUiState.Loading -> {
                    item(
                        key = "loading",
                        span = StaggeredGridItemSpan.FullLine,
                        contentType = "loading"
                    ) {
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier.fillMaxWidth(),
                        ) {
                            CircularWavyProgressIndicator()
                        }
                    }
                }

                ImageUiState.Success -> {
                    if (viewModel.imagesList.isEmpty()) {
                        item(
                            key = "empty",
                            span = StaggeredGridItemSpan.FullLine,
                            contentType = "empty"
                        ) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                            ) {
                                Text("No images found")
                            }
                        }
                    }
                }
            }

            items(
                viewModel.imagesList,
                contentType = { "image" },
                key = { it.url }
            ) { models ->

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
                    isBlacklisted = blacklisted.any { b -> b.imageUrl == models.url },
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
                        isBlacklisted = blacklisted.any { b -> b.imageUrl == models.url },
                        onClick = {
                            if (models.height < 2000 || models.width < 2000) {
                                sheetDetails = models
                            } else {
                                uriHandler.openUri(models.url)
                            }
                        },
                        onLongClick = { showDialog = true },
                        modifier = Modifier.animateItem()
                    )
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
    modifier: Modifier = Modifier
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
        modifier = modifier
            .fillMaxWidth()
            .aspectRatio(
                if (images.width > 0 && images.height > 0) {
                    images.width.toFloat() / images.height.toFloat()
                } else {
                    1f
                }
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
                    if (images.url.endsWith("mp4")) {
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier
                                .background(Color.Black)
                                .matchParentSize()
                        ) {
                            VideoThumbnailLoader(
                                videoUrl = images.url,
                                contentScale = ContentScale.Crop,
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
                            modifier = if (!showNsfw && images.nsfwLevel.canNotShow()) {
                                Modifier.blur(nsfwBlurStrength.dp)
                            } else {
                                Modifier
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