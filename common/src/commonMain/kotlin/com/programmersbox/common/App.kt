@file:Suppress("INLINE_FROM_HIGHER_PLATFORM")

package com.programmersbox.common

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.paging.LoadState
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.cachedIn
import io.kamel.image.KamelImage
import io.kamel.image.asyncPainterResource
import moe.tlaster.precompose.viewmodel.ViewModel
import moe.tlaster.precompose.viewmodel.viewModel
import moe.tlaster.precompose.viewmodel.viewModelScope

@OptIn(ExperimentalFoundationApi::class)
@Composable
internal fun App() {
    val viewModel = viewModel { CivitAiViewModel() }
    val lazyPagingItems = viewModel.pager.collectAsLazyPagingItems()
    Scaffold(
        bottomBar = {
            BottomAppBar(
                actions = {
                    Text("Models Loaded: ${lazyPagingItems.itemCount}")
                },
                floatingActionButton = {
                    FloatingActionButton(
                        onClick = { lazyPagingItems.refresh() },
                    ) {
                        Icon(Icons.Default.Refresh, null)
                    }
                }
            )
        }
    ) { padding ->
        LazyVerticalGrid(
            columns = adaptiveGridCell(),
            contentPadding = padding,
            verticalArrangement = Arrangement.spacedBy(4.dp),
            horizontalArrangement = Arrangement.spacedBy(4.dp),
            modifier = Modifier.fillMaxSize()
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
                contentType = lazyPagingItems.itemContentType()
            ) {
                lazyPagingItems[it]?.let { models ->
                    ModelItem(
                        models = models,
                        onClick = {},
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
        }
    }
}

@Composable
private fun ModelItem(
    models: Models,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    CoverCard(
        imageUrl = remember { models.modelVersions.random().images.random().url },
        name = models.name,
        type = models.type,
        isNsfw = models.nsfw,
        onClick = onClick,
        modifier = modifier.size(
            width = ComposableUtils.IMAGE_WIDTH,
            height = ComposableUtils.IMAGE_HEIGHT
        )
    )
}

@Composable
fun CoverCard(
    imageUrl: String,
    name: String,
    type: ModelType,
    isNsfw: Boolean,
    modifier: Modifier = Modifier,
    onClick: () -> Unit = {},
) {
    Surface(
        onClick = onClick,
        tonalElevation = 4.dp,
        shape = MaterialTheme.shapes.medium,
        modifier = modifier
    ) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            KamelImage(
                resource = asyncPainterResource(imageUrl),
                onLoading = {
                    CircularProgressIndicator(progress = animateFloatAsState(targetValue = it, label = "").value)
                },
                contentScale = ContentScale.FillBounds,
                contentDescription = name,
                modifier = Modifier.matchParentSize()
            )
            /*AsyncImage(
                model = ImageRequest.Builder(LocalContext.current)
                    .data(imageUrl)
                    .lifecycle(LocalLifecycleOwner.current)
                    .apply { headers.forEach { addHeader(it.key, it.value.toString()) } }
                    .crossfade(true)
                    .placeholder(placeHolder)
                    .error(error)
                    .build(),
                contentScale = ContentScale.FillBounds,
                contentDescription = name,
                modifier = Modifier.matchParentSize()
            )*/

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

            ElevatedAssistChip(
                label = { Text(type.name) },
                onClick = {},
                colors = AssistChipDefaults.elevatedAssistChipColors(
                    disabledLabelColor = MaterialTheme.colorScheme.onSurface,
                    disabledContainerColor = MaterialTheme.colorScheme.surface
                ),
                enabled = false,
                modifier = Modifier
                    .padding(horizontal = 4.dp)
                    .align(Alignment.TopStart)
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
                    modifier = Modifier
                        .padding(horizontal = 4.dp)
                        .align(Alignment.TopEnd)
                )
            }
        }
    }
}