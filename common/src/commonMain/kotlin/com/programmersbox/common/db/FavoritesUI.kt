package com.programmersbox.common.db

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowRight
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.DialogProperties
import com.programmersbox.common.*
import com.programmersbox.common.home.CardContent
import io.kamel.image.KamelImage
import io.kamel.image.asyncPainterResource
import moe.tlaster.precompose.flow.collectAsStateWithLifecycle

@OptIn(ExperimentalFoundationApi::class, ExperimentalMaterial3Api::class)
@Composable
fun FavoritesUI() {
    val navController = LocalNavController.current
    val dataStore = LocalDataStore.current
    val showNsfw by remember { dataStore.showNsfw.flow }.collectAsStateWithLifecycle(false)
    val blurStrength by remember { dataStore.hideNsfwStrength.flow }.collectAsStateWithLifecycle(6f)
    val database = LocalDatabase.current
    val list by database.getFavorites().collectAsStateWithLifecycle(emptyList())
    var search by remember { mutableStateOf("") }
    val scope = rememberCoroutineScope()
    Scaffold(
        topBar = {
            SearchBar(
                query = search,
                onQueryChange = { search = it },
                onSearch = {},
                active = false,
                onActiveChange = {},
                leadingIcon = {
                    IconButton(
                        onClick = { navController.popBackStack() }
                    ) { Icon(Icons.Default.ArrowBack, null) }
                },
                placeholder = { Text("Search Favorites") },
                trailingIcon = { Text("(${list.size})") },
                modifier = Modifier.fillMaxWidth()
            ) {}
        }
    ) { padding ->
        LazyVerticalGrid(
            columns = adaptiveGridCell(),
            verticalArrangement = Arrangement.spacedBy(4.dp),
            horizontalArrangement = Arrangement.spacedBy(4.dp),
            contentPadding = padding,
            modifier = Modifier
                .padding(4.dp)
                .fillMaxSize()
        ) {
            items(
                list.filter { it.name.contains(search, true) },
                key = { it.id }
            ) { model ->
                when (model) {
                    is FavoriteModel.Creator -> {
                        CreatorItem(
                            models = model,
                            onClick = { navController.navigateToUser(model.name) }
                        )
                    }

                    is FavoriteModel.Image -> {
                        var sheetDetails by remember { mutableStateOf<FavoriteModel.Image?>(null) }

                        sheetDetails?.let { sheetModel ->
                            SheetDetails(
                                onDismiss = { sheetDetails = null },
                                content = {
                                    SheetContent(
                                        image = sheetModel,
                                        onNavigate = { navController.navigateToDetail(sheetModel.id) }
                                    )
                                }
                            )
                        }
                        ImageItem(
                            models = model,
                            onClick = { sheetDetails = model },
                            showNsfw = showNsfw,
                            blurStrength = blurStrength.dp,
                            modifier = Modifier.animateItemPlacement()
                        )
                    }

                    is FavoriteModel.Model -> {
                        ModelItem(
                            models = model,
                            onClick = { navController.navigateToDetail(model.id) },
                            showNsfw = showNsfw,
                            blurStrength = blurStrength.dp,
                            modifier = Modifier.animateItemPlacement()
                        )
                    }
                }

            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class, ExperimentalMaterial3Api::class)
@Composable
private fun SheetContent(
    image: FavoriteModel.Image,
    onNavigate: () -> Unit,
) {
    val painter = asyncPainterResource(image.imageUrl.orEmpty())
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
                        onClick = onNavigate
                    ) { Icon(Icons.Default.ArrowRight, null) }
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
            if (image.nsfw) {
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
                        border = AssistChipDefaults.assistChipBorder(
                            disabledBorderColor = MaterialTheme.colorScheme.error,
                            borderWidth = 1.dp
                        ),
                    )
                }
            }
            image.imageMetaDb?.let { meta ->
                Column(
                    modifier = Modifier.padding(4.dp)
                ) {
                    meta.model?.let { Text("Model: $it") }
                    Divider()
                    meta.prompt?.let { Text("Prompt: $it") }
                    Divider()
                    meta.negativePrompt?.let { Text("Negative Prompt: $it") }
                    Divider()
                    meta.seed?.let { Text("Seed: $it") }
                    Divider()
                    meta.sampler?.let { Text("Sampler: $it") }
                    Divider()
                    meta.steps?.let { Text("Steps: $it") }
                    Divider()
                    meta.clipSkip?.let { Text("Clip Skip: $it") }
                    Divider()
                    meta.size?.let { Text("Size: $it") }
                    Divider()
                    meta.cfgScale?.let { Text("Cfg Scale: $it") }
                }
            }
        }
    }
}

@Composable
private fun CreatorItem(
    models: FavoriteModel.Creator,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    CoverCard(
        imageUrl = models.imageUrl.orEmpty(),
        name = models.name,
        type = ModelType.Other,
        isNsfw = false,
        showNsfw = true,
        blurStrength = 0.dp,
        onClick = onClick,
        modifier = modifier.size(
            width = ComposableUtils.IMAGE_WIDTH,
            height = ComposableUtils.IMAGE_HEIGHT
        )
    )
}

@Composable
private fun ImageItem(
    models: FavoriteModel.Image,
    showNsfw: Boolean,
    blurStrength: Dp,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    CoverCard(
        imageUrl = models.imageUrl.orEmpty(),
        name = models.name,
        type = ModelType.Other,
        isNsfw = models.nsfw,
        showNsfw = showNsfw,
        blurStrength = blurStrength,
        onClick = onClick,
        modifier = modifier.size(
            width = ComposableUtils.IMAGE_WIDTH,
            height = ComposableUtils.IMAGE_HEIGHT
        )
    )
}

@Composable
private fun ModelItem(
    models: FavoriteModel.Model,
    showNsfw: Boolean,
    blurStrength: Dp,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    CoverCard(
        imageUrl = models.imageUrl.orEmpty(),
        name = models.name,
        type = ModelType.valueOf(models.type),
        isNsfw = models.nsfw,
        showNsfw = showNsfw,
        blurStrength = blurStrength,
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
    blurStrength: Dp,
    modifier: Modifier = Modifier,
    onClick: () -> Unit = {},
) {
    Surface(
        onClick = onClick,
        tonalElevation = 4.dp,
        shape = MaterialTheme.shapes.medium,
        modifier = modifier
    ) {
        CardContent(
            imageUrl = imageUrl,
            name = name,
            type = type,
            isNsfw = isNsfw,
            showNsfw = showNsfw,
            blurStrength = blurStrength
        )
    }
}