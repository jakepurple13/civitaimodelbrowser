@file:Suppress("INLINE_FROM_HIGHER_PLATFORM")

package com.programmersbox.common

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.toggleable
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.unit.dp
import com.programmersbox.common.components.LoadingImage
import com.programmersbox.common.db.FavoritesDatabase
import io.kamel.image.KamelImage
import io.kamel.image.asyncPainterResource
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import moe.tlaster.precompose.flow.collectAsStateWithLifecycle
import moe.tlaster.precompose.viewmodel.ViewModel
import moe.tlaster.precompose.viewmodel.viewModel
import moe.tlaster.precompose.viewmodel.viewModelScope
import org.jetbrains.compose.resources.ExperimentalResourceApi
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CivitAiDetailScreen(
    network: Network,
    id: String?,
    onShareClick: (String) -> Unit,
) {
    val database = LocalDatabase.current
    val viewModel = viewModel { CivitAiDetailViewModel(network, id, database) }
    val navController = LocalNavController.current
    val simpleDateTimeFormatter = remember { SimpleDateFormat("MM/dd/yy HH:mm", Locale.getDefault()) }
    val dataStore = LocalDataStore.current
    val showNsfw by remember { dataStore.showNsfw.flow }.collectAsStateWithLifecycle(false)
    val nsfwBlurStrength by remember { dataStore.hideNsfwStrength.flow }.collectAsStateWithLifecycle(6f)

    when (val model = viewModel.models) {
        is DetailViewState.Content -> {
            var sheetDetails by remember { mutableStateOf<ModelImage?>(null) }

            sheetDetails?.let { sheetModel ->
                SheetDetails(
                    sheetDetails = sheetModel,
                    onDismiss = { sheetDetails = null },
                    content = { SheetContent(it) }
                )
            }

            val scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior()
            Scaffold(
                topBar = {
                    TopAppBar(
                        title = { Text(model.models.name) },
                        navigationIcon = {
                            IconButton(
                                onClick = { navController.popBackStack() }
                            ) { Icon(Icons.Default.ArrowBack, null) }
                        },
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
                            IconButton(
                                onClick = { onShareClick(viewModel.modelUrl) }
                            ) { Icon(Icons.Default.Share, null) }

                            val uriHandler = LocalUriHandler.current
                            IconButton(
                                onClick = { uriHandler.openUri(viewModel.modelUrl) }
                            ) { Icon(Icons.Default.OpenInBrowser, null) }
                        }
                    )
                },
                modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection)
            ) { paddingValues ->
                LazyVerticalGrid(
                    columns = adaptiveGridCell(),
                    contentPadding = paddingValues,
                    verticalArrangement = Arrangement.spacedBy(4.dp),
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    item(
                        span = { GridItemSpan(maxLineSpan) }
                    ) {
                        ListItem(
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
                                        border = AssistChipDefaults.assistChipBorder(
                                            disabledBorderColor = MaterialTheme.colorScheme.error,
                                            borderWidth = 1.dp
                                        ),
                                        enabled = false,
                                    )
                                }
                            },
                            modifier = Modifier.animateContentSize()
                        )
                    }

                    model.models.modelVersions.forEach { version ->
                        item(
                            span = { GridItemSpan(maxLineSpan) }
                        ) {
                            var showMoreInfo by remember { mutableStateOf(false) }
                            Column {
                                TopAppBar(
                                    title = { Text("Version: ${version.name}") },
                                    actions = {
                                        IconButton(
                                            onClick = { showMoreInfo = !showMoreInfo }
                                        ) {
                                            Icon(
                                                Icons.Filled.ArrowDropDown,
                                                null,
                                                Modifier.rotate(if (showMoreInfo) 180f else 0f)
                                            )
                                        }
                                    },
                                    windowInsets = WindowInsets(0.dp)
                                )
                                AnimatedVisibility(showMoreInfo) {
                                    ListItem(
                                        headlineContent = {
                                            Text("Last Update at: " + simpleDateTimeFormatter.format(version.updatedAt.toEpochMilliseconds()))
                                        },
                                        supportingContent = version.parsedDescription()?.let { { Text(it) } }
                                    )
                                }
                            }
                        }

                        items(version.images) { images ->
                            ImageCard(
                                images = images,
                                showNsfw = showNsfw,
                                nsfwBlurStrength = nsfwBlurStrength,
                                onClick = { sheetDetails = images }
                            )
                        }
                    }
                }
            }
        }

        DetailViewState.Error -> {
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

@OptIn(ExperimentalResourceApi::class)
@Composable
private fun ImageCard(
    images: ModelImage,
    showNsfw: Boolean,
    nsfwBlurStrength: Float,
    onClick: () -> Unit,
) {
    Surface(
        tonalElevation = 4.dp,
        shape = MaterialTheme.shapes.medium,
        border = if (images.nsfw.canNotShow())
            BorderStroke(1.dp, MaterialTheme.colorScheme.error)
        else null,
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

@Composable
private fun SheetContent(image: ModelImage) {
    SelectionContainer {
        Column(
            modifier = Modifier.verticalScroll(rememberScrollState())
        ) {
            KamelImage(
                resource = asyncPainterResource(image.url),
                onLoading = {
                    CircularProgressIndicator(
                        progress = animateFloatAsState(
                            targetValue = it,
                            label = ""
                        ).value
                    )
                },
                contentDescription = null,
                modifier = Modifier.align(Alignment.CenterHorizontally)
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
                        border = AssistChipDefaults.assistChipBorder(
                            disabledBorderColor = MaterialTheme.colorScheme.error,
                            borderWidth = 1.dp
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
                        border = AssistChipDefaults.assistChipBorder(
                            disabledBorderColor = MaterialTheme.colorScheme.error,
                            borderWidth = 1.dp
                        ),
                    )
                }
            }
            image.meta?.let { meta ->
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

class CivitAiDetailViewModel(
    private val network: Network,
    private val id: String?,
    private val database: FavoritesDatabase,
) : ViewModel() {
    val modelUrl = "https://civitai.com/models/$id"
    var models by mutableStateOf<DetailViewState>(DetailViewState.Loading)
    var isFavorite by mutableStateOf(false)

    init {
        loadData()
        database.getFavorites()
            .onEach { m -> isFavorite = m.any { it.id == id?.toLongOrNull() } }
            .launchIn(viewModelScope)
    }

    fun loadData() {
        viewModelScope.launch {
            models = DetailViewState.Loading
            models = id?.let { network.fetchModel(it) }
                ?.onFailure { it.printStackTrace() }
                ?.fold(
                    onSuccess = { DetailViewState.Content(it) },
                    onFailure = { DetailViewState.Error }
                ) ?: DetailViewState.Error
        }
    }

    fun addToFavorites() {
        viewModelScope.launch {
            (models as? DetailViewState.Content)?.models?.let { m ->
                database.addFavorite(
                    id = m.id,
                    name = m.name,
                    description = m.description,
                    type = m.type,
                    nsfw = m.nsfw,
                    imageUrl = m.modelVersions.firstOrNull()?.images?.firstOrNull()?.url
                )
            }
        }
    }

    fun removeFromFavorites() {
        viewModelScope.launch {
            (models as? DetailViewState.Content)?.models?.id?.let { database.removeFavorite(it) }
        }
    }
}

sealed class DetailViewState {
    data object Loading : DetailViewState()
    data object Error : DetailViewState()
    data class Content(val models: Models) : DetailViewState()
}