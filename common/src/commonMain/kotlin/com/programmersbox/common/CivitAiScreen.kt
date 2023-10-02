@file:Suppress("INLINE_FROM_HIGHER_PLATFORM")

package com.programmersbox.common

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.paging.LoadState
import io.kamel.image.KamelImage
import io.kamel.image.asyncPainterResource
import moe.tlaster.precompose.viewmodel.viewModel

@OptIn(ExperimentalFoundationApi::class, ExperimentalMaterial3Api::class)
@Composable
fun CivitAiScreen(
    network: Network,
) {
    val viewModel = viewModel { CivitAiViewModel(network) }
    val navController = LocalNavController.current
    val lazyPagingItems = viewModel.pager.collectAsLazyPagingItems()
    val scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior()
    val showNsfw by remember { DataStore.showNsfw.flow }.collectAsState(false)
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("CivitAi Model Browser") },
                actions = {
                    IconButton(
                        onClick = { navController.navigate(Screen.Settings.routeId) }
                    ) { Icon(Icons.Default.Settings, null) }
                },
                scrollBehavior = scrollBehavior
            )
        },
        bottomBar = {
            BottomAppBar(
                actions = {
                    Text("Models Loaded: ${lazyPagingItems.itemCount}")
                },
                floatingActionButton = {
                    FloatingActionButton(
                        onClick = { lazyPagingItems.refresh() },
                    ) { Icon(Icons.Default.Refresh, null) }
                }
            )
        },
        modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection)
    ) { padding ->
        LazyVerticalGrid(
            columns = adaptiveGridCell(),
            contentPadding = padding,
            verticalArrangement = Arrangement.spacedBy(4.dp),
            horizontalArrangement = Arrangement.spacedBy(4.dp),
            modifier = Modifier
                .padding(4.dp)
                .fillMaxSize()
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
                        onClick = {
                            navController.navigate(Screen.Detail.routeId.replace("{modelId}", models.id.toString()))
                        },
                        showNsfw = showNsfw,
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
    showNsfw: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    CoverCard(
        imageUrl = remember { models.modelVersions.randomOrNull()?.images?.randomOrNull()?.url.orEmpty() },
        name = models.name,
        type = models.type,
        isNsfw = models.nsfw,
        showNsfw = showNsfw,
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
    showNsfw: Boolean,
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
                modifier = Modifier
                    .matchParentSize()
                    .let {
                        if (!showNsfw && isNsfw) {
                            it.blur(DataStore.hideNsfwStrength.flow.collectAsState(6f).value.dp)
                        } else {
                            it
                        }
                    }
            )

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