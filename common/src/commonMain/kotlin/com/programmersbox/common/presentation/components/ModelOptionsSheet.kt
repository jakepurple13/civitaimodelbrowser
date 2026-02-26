package com.programmersbox.common.presentation.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.layout.MutableIntervalList
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.PlaylistAdd
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Block
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Preview
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ElevatedAssistChip
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ElevatedFilterChip
import androidx.compose.material3.ElevatedSuggestionChip
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.ListItemShapes
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SegmentedListItem
import androidx.compose.material3.SuggestionChipDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveableStateHolder
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.compose.dropUnlessResumed
import com.programmersbox.common.CloseButton
import com.programmersbox.common.ModelType
import com.programmersbox.common.Models
import com.programmersbox.common.db.BlacklistedItemRoom
import com.programmersbox.common.db.CustomList
import com.programmersbox.common.db.FavoriteType
import com.programmersbox.common.db.FavoritesDao
import com.programmersbox.common.db.ListRepository
import com.programmersbox.common.presentation.home.BlacklistHandling
import com.programmersbox.common.presentation.qrcode.QrCodeType
import com.programmersbox.common.presentation.qrcode.ShareViaQrCode
import com.programmersbox.resources.Res
import com.programmersbox.resources.add_to_list
import com.programmersbox.resources.blacklist
import com.programmersbox.resources.cancel
import com.programmersbox.resources.choose_a_list
import com.programmersbox.resources.confirm
import com.programmersbox.resources.create_new_list
import com.programmersbox.resources.creators
import com.programmersbox.resources.favorite_model
import com.programmersbox.resources.list_name
import com.programmersbox.resources.made_by
import com.programmersbox.resources.nsfw
import com.programmersbox.resources.open
import com.programmersbox.resources.open_images
import com.programmersbox.resources.share
import com.programmersbox.resources.unblacklist
import com.programmersbox.resources.unfavorite_model
import com.programmersbox.resources.view_creators_models
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.koinInject

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun ModelOptionsSheet(
    id: Long,
    imageUrl: String?,
    hash: String?,
    name: String?,
    type: String?,
    description: String?,
    nsfw: Boolean,
    creatorName: String?,
    creatorImage: String?,
    showSheet: Boolean,
    onDialogDismiss: () -> Unit,
    onNavigateToDetail: (String) -> Unit,
    onNavigateToUser: ((String) -> Unit)? = null,
) {
    if (showSheet) {
        val dao = koinInject<FavoritesDao>()
        val isFavorite by dao
            .getFavoritesByType(FavoriteType.Model, id)
            .collectAsStateWithLifecycle(false)
        val listRepository = koinInject<ListRepository>()
        val scope = rememberCoroutineScope()

        val sheetState = rememberModalBottomSheetState(
            skipPartiallyExpanded = true
        )

        val colors =
            ListItemDefaults.colors(containerColor = MaterialTheme.colorScheme.surfaceContainer)

        val modelOptionsScope = rememberModelOptionsScope {
            group {
                add {
                    SegmentedListItem(
                        leadingContent = { Icon(Icons.Default.Preview, null) },
                        content = { Text(stringResource(Res.string.open)) },
                        onClick = {
                            onNavigateToDetail(id.toString())
                            scope.launch { sheetState.hide() }
                                .invokeOnCompletion { onDialogDismiss() }
                        },
                        shapes = it,
                        colors = colors
                    )
                }

                onNavigateToUser?.let { onNav ->
                    add {
                        SegmentedListItem(
                            leadingContent = {
                                creatorImage?.let { image ->
                                    LoadingImage(
                                        image,
                                        contentScale = ContentScale.FillBounds,
                                        modifier = Modifier
                                            .size(24.dp)
                                            .clip(CircleShape)
                                    )
                                }
                                    ?: Icon(Icons.Default.Person, null)
                            },
                            content = {
                                Text(
                                    stringResource(
                                        Res.string.view_creators_models,
                                        creatorName ?: stringResource(Res.string.creators)
                                    )
                                )
                            },
                            onClick = {
                                creatorName?.let { p1 -> onNav(p1) }
                                scope.launch { sheetState.hide() }
                                    .invokeOnCompletion { onDialogDismiss() }
                            },
                            shapes = it,
                            colors = colors
                        )
                    }
                }
            }

            item {
                var showQrCode by remember { mutableStateOf(false) }

                if (showQrCode) {
                    ShareViaQrCode(
                        title = name.orEmpty(),
                        url = "https://civitai.com/models/$id",
                        qrCodeType = QrCodeType.Model,
                        id = id.toString(),
                        username = "",
                        imageUrl = imageUrl.orEmpty(),
                        onClose = { showQrCode = false }
                    )
                }

                ListItem(
                    leadingContent = { Icon(Icons.Default.Share, null) },
                    content = { Text(stringResource(Res.string.share)) },
                    colors = colors,
                    shapes = it,
                    onClick = { showQrCode = true }
                )
            }

            group {
                add {
                    if (isFavorite) {
                        SegmentedListItem(
                            leadingContent = { Icon(Icons.Default.Favorite, null) },
                            content = { Text(stringResource(Res.string.unfavorite_model)) },
                            colors = colors,
                            shapes = it,
                            onClick = {
                                scope.launch {
                                    dao.removeModel(id)
                                    sheetState.hide()
                                }.invokeOnCompletion { onDialogDismiss() }
                            },
                        )
                    } else {
                        SegmentedListItem(
                            leadingContent = { Icon(Icons.Default.Favorite, null) },
                            content = { Text(stringResource(Res.string.favorite_model)) },
                            onClick = {
                                scope.launch {
                                    dao.addFavorite(
                                        id = id,
                                        name = name.orEmpty(),
                                        description = description,
                                        type = runCatching { ModelType.valueOf(type!!) }
                                            .getOrElse { ModelType.Other },
                                        nsfw = nsfw,
                                        imageUrl = imageUrl,
                                        favoriteType = FavoriteType.Model,
                                        modelId = id,
                                        hash = hash,
                                        creatorName = creatorName,
                                        creatorImage = creatorImage
                                    )
                                    sheetState.hide()
                                }.invokeOnCompletion { onDialogDismiss() }
                            },
                            shapes = it,
                            colors = colors
                        )
                    }
                }

                add {
                    var showLists by remember { mutableStateOf(false) }
                    val listState = rememberModalBottomSheetState(true)

                    if (showLists) {
                        ModalBottomSheet(
                            onDismissRequest = { showLists = false },
                            containerColor = MaterialTheme.colorScheme.surface,
                            sheetState = listState
                        ) {
                            ListChoiceScreen(
                                id = id,
                                onAdd = { selectedLists ->
                                    scope.launch {
                                        selectedLists.forEach { item ->
                                            listRepository.addToList(
                                                uuid = item.item.uuid,
                                                id = id,
                                                name = name.orEmpty(),
                                                description = description,
                                                type = type.orEmpty(),
                                                nsfw = nsfw,
                                                imageUrl = imageUrl,
                                                favoriteType = FavoriteType.Model,
                                                hash = hash,
                                                creatorName = creatorName,
                                                creatorImage = creatorImage,
                                            )
                                        }
                                        listState.hide()
                                    }.invokeOnCompletion { showLists = false }
                                },
                                navigationIcon = {
                                    IconButton(
                                        onClick = { showLists = false }
                                    ) { Icon(Icons.Default.Close, null) }
                                },
                            )
                        }
                    }

                    SegmentedListItem(
                        leadingContent = { Icon(Icons.AutoMirrored.Filled.PlaylistAdd, null) },
                        content = { Text(stringResource(Res.string.add_to_list)) },
                        colors = colors,
                        shapes = it,
                        onClick = { showLists = true }
                    )
                }
            }
        }

        ModalBottomSheet(
            onDismissRequest = onDialogDismiss,
            sheetState = sheetState,
            containerColor = MaterialTheme.colorScheme.surface,
        ) {
            Column(
                modifier = Modifier.verticalScroll(rememberScrollState())
            ) {
                TopAppBar(
                    title = { Text(name.orEmpty()) },
                )

                FlowRow(
                    itemVerticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    modifier = Modifier.padding(8.dp)
                ) {
                    ElevatedSuggestionChip(
                        label = { Text(type.orEmpty()) },
                        onClick = {},
                        colors = SuggestionChipDefaults.elevatedSuggestionChipColors(
                            disabledLabelColor = MaterialTheme.colorScheme.onSurface,
                            disabledContainerColor = MaterialTheme.colorScheme.surfaceContainerLow
                        ),
                        border = BorderStroke(
                            1.dp,
                            MaterialTheme.colorScheme.onSurface
                        ),
                        enabled = false,
                    )

                    if (nsfw) {
                        ElevatedAssistChip(
                            label = { Text(stringResource(Res.string.nsfw)) },
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

                val stateHolder = rememberSaveableStateHolder()

                Column(
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.padding(8.dp)
                ) {
                    for (index in 0 until modelOptionsScope.size) {
                        stateHolder.SaveableStateProvider(index) {
                            when (val item = modelOptionsScope[index]) {
                                is ModelOptionsType.Group -> {
                                    Column(
                                        verticalArrangement = Arrangement.spacedBy(ListItemDefaults.SegmentedGap),
                                    ) {
                                        item.content.forEachIndexed { itemIndex, function ->
                                            function(
                                                ListItemDefaults.segmentedShapes(
                                                    index = itemIndex,
                                                    count = item.content.size
                                                )
                                            )
                                        }
                                    }
                                }

                                is ModelOptionsType.Item -> item.content(
                                    ListItemDefaults.shapes(
                                        shape = MaterialTheme.shapes.large
                                    )
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun ListChoiceScreen(
    id: Long,
    navigationIcon: @Composable () -> Unit = { CloseButton() },
    onAdd: (List<CustomList>) -> Unit,
) {
    val listRepository = koinInject<ListRepository>()
    val scope = rememberCoroutineScope()
    val list by listRepository
        .getAllLists()
        .collectAsStateWithLifecycle(emptyList())

    var selectedLists by remember { mutableStateOf<Set<CustomList>>(emptySet()) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(Res.string.choose_a_list)) },
                navigationIcon = navigationIcon,
                actions = {
                    if (list.isNotEmpty()) {
                        Text("(${selectedLists.size}/${list.size})")
                    }
                }
            )
        },
        floatingActionButton = {
            if (selectedLists.isNotEmpty()) {
                FloatingActionButton(
                    onClick = { onAdd(selectedLists.toList()) }
                ) {
                    Icon(Icons.Default.Check, null)
                }
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .padding(paddingValues)
                .navigationBarsPadding()
        ) {
            var showAdd by remember { mutableStateOf(false) }
            ElevatedCard(
                onClick = { showAdd = !showAdd },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            ) {
                ListItem(
                    headlineContent = {
                        Text(
                            stringResource(Res.string.create_new_list),
                            style = MaterialTheme.typography.titleLarge
                        )
                    },
                    trailingContent = { Icon(Icons.Default.Add, null) }
                )
            }
            if (showAdd) {
                var name by remember { mutableStateOf("") }
                var description by remember { mutableStateOf("") }
                AlertDialog(
                    onDismissRequest = { showAdd = false },
                    title = { Text(stringResource(Res.string.create_new_list)) },
                    text = {
                        Column(
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            OutlinedTextField(
                                value = name,
                                onValueChange = { name = it },
                                label = { Text(stringResource(Res.string.list_name)) },
                                singleLine = true,
                                modifier = Modifier.fillMaxWidth()
                            )
                            OutlinedTextField(
                                value = description,
                                onValueChange = { description = it },
                                label = { Text("Description (optional)") },
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    },
                    confirmButton = {
                        TextButton(
                            onClick = {
                                scope.launch {
                                    listRepository.createList(
                                        name = name,
                                        description = description.takeIf { it.isNotBlank() }
                                    )
                                    showAdd = false
                                }
                            },
                            enabled = name.isNotEmpty()
                        ) { Text(stringResource(Res.string.confirm)) }
                    },
                    dismissButton = {
                        TextButton(
                            onClick = { showAdd = false }
                        ) { Text(stringResource(Res.string.cancel)) }
                    }
                )
            }

            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(2.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                items(list, key = { it.item.uuid }) { item ->
                    val isSelected = item in selectedLists
                    val isInList = item.list.find { l -> l.id == id } != null
                    ListItem(
                        leadingContent = {
                            Checkbox(
                                checked = isSelected,
                                onCheckedChange = null
                            )
                        },
                        trailingContent = {
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text("(${item.list.size})")
                                if (isInList) {
                                    Icon(Icons.Default.Check, null)
                                }
                            }
                        },
                        enabled = !isInList,
                        checked = isSelected,
                        onCheckedChange = {
                            selectedLists = if (it) {
                                selectedLists + item
                            } else {
                                selectedLists - item
                            }
                        },
                    ) { Text(item.item.name) }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ListChoiceScreen(
    username: String,
    onAdd: (List<CustomList>) -> Unit,
    navigationIcon: @Composable () -> Unit = { CloseButton() },
) {
    val listRepository = koinInject<ListRepository>()
    val scope = rememberCoroutineScope()
    val list by listRepository
        .getAllLists()
        .collectAsStateWithLifecycle(emptyList())

    var selectedLists by remember { mutableStateOf<Set<CustomList>>(emptySet()) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(Res.string.choose_a_list)) },
                navigationIcon = navigationIcon,
                actions = {
                    if (list.isNotEmpty()) {
                        Text("(${selectedLists.size}/${list.size})")
                    }
                }
            )
        },
        floatingActionButton = {
            if (selectedLists.isNotEmpty()) {
                FloatingActionButton(
                    onClick = { onAdd(selectedLists.toList()) }
                ) {
                    Icon(Icons.Default.Check, null)
                }
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .padding(paddingValues)
                .navigationBarsPadding()
        ) {
            var showAdd by remember { mutableStateOf(false) }
            ElevatedCard(
                onClick = { showAdd = !showAdd },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            ) {
                ListItem(
                    headlineContent = {
                        Text(
                            stringResource(Res.string.create_new_list),
                            style = MaterialTheme.typography.titleLarge
                        )
                    },
                    trailingContent = { Icon(Icons.Default.Add, null) }
                )
            }
            if (showAdd) {
                var name by remember { mutableStateOf("") }
                var description by remember { mutableStateOf("") }
                AlertDialog(
                    onDismissRequest = { showAdd = false },
                    title = { Text(stringResource(Res.string.create_new_list)) },
                    text = {
                        Column(
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            OutlinedTextField(
                                value = name,
                                onValueChange = { name = it },
                                label = { Text(stringResource(Res.string.list_name)) },
                                singleLine = true,
                                modifier = Modifier.fillMaxWidth()
                            )
                            OutlinedTextField(
                                value = description,
                                onValueChange = { description = it },
                                label = { Text("Description (optional)") },
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    },
                    confirmButton = {
                        TextButton(
                            onClick = {
                                scope.launch {
                                    listRepository.createList(
                                        name = name,
                                        description = description.takeIf { it.isNotBlank() },
                                        showToast = false
                                    )
                                    showAdd = false
                                }
                            },
                            enabled = name.isNotEmpty()
                        ) { Text(stringResource(Res.string.confirm)) }
                    },
                    dismissButton = {
                        TextButton(
                            onClick = { showAdd = false }
                        ) { Text(stringResource(Res.string.cancel)) }
                    }
                )
            }

            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(2.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                items(list, key = { it.item.uuid }) { item ->
                    val isSelected = item in selectedLists
                    val isInList = item.list.find { l -> l.name == username } != null
                    ListItem(
                        modifier = Modifier.clickable {
                            selectedLists = if (isSelected) {
                                selectedLists - item
                            } else {
                                selectedLists + item
                            }
                        },
                        leadingContent = {
                            Checkbox(
                                checked = isSelected,
                                onCheckedChange = null
                            )
                        },
                        headlineContent = { Text(item.item.name) },
                        trailingContent = {
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text("(${item.list.size})")
                                if (isInList) {
                                    Icon(Icons.Default.Check, null)
                                }
                            }
                        }
                    )
                }
            }
        }
    }
}

sealed class ModelOptionsType {

    @OptIn(ExperimentalMaterial3ExpressiveApi::class)
    class Item(
        val content: @Composable (ListItemShapes) -> Unit
    ) : ModelOptionsType()

    @OptIn(ExperimentalMaterial3ExpressiveApi::class)
    class Group(
        val content: MutableList<@Composable (ListItemShapes) -> Unit>
    ) : ModelOptionsType()
}

interface ModelOptionsScope {
    fun item(
        @OptIn(ExperimentalMaterial3ExpressiveApi::class)
        content: @Composable (ListItemShapes) -> Unit
    )

    fun group(
        @OptIn(ExperimentalMaterial3ExpressiveApi::class)
        content: MutableList<@Composable (ListItemShapes) -> Unit>.() -> Unit
    )

    val size: Int

    operator fun get(index: Int): ModelOptionsType
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
private fun ModelOptionsScopeImpl(
    content: ModelOptionsScope.() -> Unit = {},
): ModelOptionsScope {
    val intervals: MutableIntervalList<ModelOptionsType> =
        remember { MutableIntervalList() }

    return remember(content) {
        object : ModelOptionsScope {
            override val size: Int get() = intervals.size
            override fun get(index: Int) = intervals[index].value

            override fun item(
                content: @Composable (ListItemShapes) -> Unit
            ) {
                intervals.addInterval(1, ModelOptionsType.Item(content))
            }

            override fun group(
                content: MutableList<@Composable (ListItemShapes) -> Unit>.() -> Unit
            ) {
                intervals.addInterval(
                    1,
                    ModelOptionsType.Group(
                        mutableListOf<@Composable (ListItemShapes) -> Unit>()
                            .apply(content)
                    )
                )
            }

        }.apply(content)
    }
}


@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun ModelOptionsSheet(
    models: Models,
    blacklisted: List<BlacklistedItemRoom>,
    isBlacklisted: Boolean,
    showSheet: Boolean,
    onDialogDismiss: () -> Unit,
    onNavigateToDetail: (String) -> Unit,
    onNavigateToUser: ((String) -> Unit)? = null,
    onNavigateToDetailImages: ((Long, String) -> Unit)? = null,
) {
    if (showSheet) {
        val dao = koinInject<FavoritesDao>()
        val isFavorite by dao
            .getFavoritesByType(FavoriteType.Model, models.id)
            .collectAsStateWithLifecycle(false)
        val listRepository = koinInject<ListRepository>()
        val scope = rememberCoroutineScope()
        var showDialog by remember { mutableStateOf(false) }

        val sheetState = rememberModalBottomSheetState(
            skipPartiallyExpanded = true
        )

        BlacklistHandling(
            blacklisted = blacklisted,
            modelId = models.id,
            name = models.name,
            nsfw = models.nsfw,
            showDialog = showDialog,
            onDialogDismiss = {
                showDialog = false
                scope.launch { sheetState.hide() }
                    .invokeOnCompletion { onDialogDismiss() }
            }
        )

        val modelImage = remember(models) {
            models
                .modelVersions
                .firstOrNull { it.images.isNotEmpty() }
                ?.images
                ?.firstOrNull { it.url.isNotEmpty() }
        }

        val colors =
            ListItemDefaults.colors(containerColor = MaterialTheme.colorScheme.surfaceContainer)

        val modelOptionsScope = rememberModelOptionsScope {
            item {
                if (isBlacklisted) {
                    ListItem(
                        leadingContent = { Icon(Icons.Default.Block, null) },
                        content = { Text(stringResource(Res.string.unblacklist)) },
                        onClick = { showDialog = true },
                        shapes = it,
                        colors = colors
                    )
                } else {
                    ListItem(
                        leadingContent = { Icon(Icons.Default.Block, null) },
                        content = { Text(stringResource(Res.string.blacklist)) },
                        onClick = { showDialog = true },
                        shapes = it,
                        colors = colors
                    )
                }
            }

            group {
                add {
                    SegmentedListItem(
                        leadingContent = { Icon(Icons.Default.Preview, null) },
                        content = { Text(stringResource(Res.string.open)) },
                        onClick = dropUnlessResumed {
                            onNavigateToDetail(models.id.toString())
                            scope.launch { sheetState.hide() }
                                .invokeOnCompletion { onDialogDismiss() }
                        },
                        shapes = it,
                        colors = colors
                    )
                }

                onNavigateToUser?.let { onNav ->
                    add {
                        SegmentedListItem(
                            leadingContent = {
                                models
                                    .creator
                                    ?.image
                                    ?.let { image ->
                                        LoadingImage(
                                            image,
                                            contentScale = ContentScale.FillBounds,
                                            modifier = Modifier
                                                .size(24.dp)
                                                .clip(CircleShape)
                                        )
                                    }
                                    ?: Icon(Icons.Default.Person, null)
                            },
                            content = {
                                Text(
                                    stringResource(
                                        Res.string.view_creators_models,
                                        models.creator?.username
                                            ?: stringResource(Res.string.creators)
                                    )
                                )
                            },
                            onClick = dropUnlessResumed {
                                onNav(models.creator?.username.orEmpty())
                                scope.launch { sheetState.hide() }
                                    .invokeOnCompletion { onDialogDismiss() }
                            },
                            shapes = it,
                            colors = colors
                        )
                    }
                }

                onNavigateToDetailImages?.let { onNav ->
                    add {
                        SegmentedListItem(
                            leadingContent = { Icon(Icons.Default.Image, null) },
                            content = { Text(stringResource(Res.string.open_images)) },
                            onClick = dropUnlessResumed {
                                onNav(models.id, models.name)
                                scope.launch { sheetState.hide() }
                                    .invokeOnCompletion { onDialogDismiss() }
                            },
                            shapes = it,
                            colors = colors
                        )
                    }
                }
            }

            group {
                add {
                    var showQrCode by remember { mutableStateOf(false) }

                    if (showQrCode) {
                        ShareViaQrCode(
                            title = models.name,
                            url = "https://civitai.com/models/${models.id}",
                            qrCodeType = QrCodeType.Model,
                            id = models.id.toString(),
                            username = models.creator?.username,
                            imageUrl = modelImage?.url.orEmpty(),
                            onClose = { showQrCode = false }
                        )
                    }

                    SegmentedListItem(
                        leadingContent = { Icon(Icons.Default.Share, null) },
                        content = { Text(stringResource(Res.string.share)) },
                        shapes = it,
                        onClick = { showQrCode = true },
                        colors = colors
                    )
                }

                add {
                    if (isFavorite) {
                        SegmentedListItem(
                            leadingContent = { Icon(Icons.Default.Favorite, null) },
                            content = { Text(stringResource(Res.string.unfavorite_model)) },
                            shapes = it,
                            onClick = {
                                scope.launch {
                                    dao.removeModel(models.id)
                                    sheetState.hide()
                                }.invokeOnCompletion { onDialogDismiss() }
                            },
                            colors = colors
                        )
                    } else {
                        SegmentedListItem(
                            leadingContent = { Icon(Icons.Default.Favorite, null) },
                            content = { Text(stringResource(Res.string.favorite_model)) },
                            shapes = it,
                            colors = colors,
                            onClick = {
                                scope.launch {
                                    dao.addFavorite(
                                        id = models.id,
                                        name = models.name,
                                        description = models.description,
                                        type = models.type,
                                        nsfw = models.nsfw,
                                        imageUrl = modelImage?.url,
                                        favoriteType = FavoriteType.Model,
                                        modelId = models.id,
                                        hash = modelImage?.hash,
                                        creatorName = models.creator?.username,
                                        creatorImage = models.creator?.image,
                                    )
                                    sheetState.hide()
                                }.invokeOnCompletion { onDialogDismiss() }
                            },
                        )
                    }
                }

                add {
                    var showLists by remember { mutableStateOf(false) }
                    val listState = rememberModalBottomSheetState(true)

                    if (showLists) {
                        ModalBottomSheet(
                            onDismissRequest = { showLists = false },
                            containerColor = MaterialTheme.colorScheme.surface,
                            sheetState = listState
                        ) {
                            ListChoiceScreen(
                                id = models.id,
                                onAdd = { selectedLists ->
                                    scope.launch {
                                        selectedLists.forEach { item ->
                                            listRepository.addToList(
                                                uuid = item.item.uuid,
                                                id = models.id,
                                                name = models.name,
                                                description = models.description,
                                                type = models.type.name,
                                                nsfw = models.nsfw,
                                                imageUrl = modelImage?.url,
                                                favoriteType = FavoriteType.Model,
                                                hash = modelImage?.hash,
                                                creatorName = models.creator?.username,
                                                creatorImage = models.creator?.image,
                                            )
                                        }
                                        listState.hide()
                                    }.invokeOnCompletion { showLists = false }
                                },
                                navigationIcon = {
                                    IconButton(
                                        onClick = { showLists = false }
                                    ) { Icon(Icons.Default.Close, null) }
                                },
                            )
                        }
                    }

                    SegmentedListItem(
                        leadingContent = { Icon(Icons.AutoMirrored.Filled.PlaylistAdd, null) },
                        content = { Text(stringResource(Res.string.add_to_list)) },
                        shapes = it,
                        onClick = { showLists = true },
                        colors = colors
                    )
                }
            }
        }

        ModalBottomSheet(
            onDismissRequest = onDialogDismiss,
            sheetState = sheetState,
            containerColor = MaterialTheme.colorScheme.surface,
        ) {
            Column(
                modifier = Modifier.verticalScroll(rememberScrollState())
            ) {
                TopAppBar(
                    title = { Text(models.name) },
                    subtitle = {
                        models
                            .creator
                            ?.username
                            ?.let { Text(stringResource(Res.string.made_by, it)) }
                    }
                )

                FlowRow(
                    itemVerticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    modifier = Modifier.padding(8.dp)
                ) {
                    ElevatedSuggestionChip(
                        label = { Text(models.type.name) },
                        onClick = {},
                        colors = SuggestionChipDefaults.elevatedSuggestionChipColors(
                            disabledLabelColor = MaterialTheme.colorScheme.onSurface,
                            disabledContainerColor = MaterialTheme.colorScheme.surfaceContainerLow
                        ),
                        border = BorderStroke(
                            1.dp,
                            MaterialTheme.colorScheme.onSurface
                        ),
                        enabled = false,
                    )

                    if (models.nsfw) {
                        ElevatedAssistChip(
                            label = { Text(stringResource(Res.string.nsfw)) },
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

                    models.tags.forEach { tag ->
                        ElevatedFilterChip(
                            label = { Text(tag) },
                            onClick = {},
                            colors = FilterChipDefaults.elevatedFilterChipColors(
                                disabledLabelColor = MaterialTheme.colorScheme.onSurface,
                                disabledContainerColor = MaterialTheme.colorScheme.surfaceContainerLow
                            ),
                            enabled = false,
                            selected = true
                        )
                    }
                }

                val stateHolder = rememberSaveableStateHolder()

                Column(
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.padding(8.dp)
                ) {
                    for (index in 0 until modelOptionsScope.size) {
                        stateHolder.SaveableStateProvider(index) {
                            when (val item = modelOptionsScope[index]) {
                                is ModelOptionsType.Group -> {
                                    Column(
                                        verticalArrangement = Arrangement.spacedBy(ListItemDefaults.SegmentedGap),
                                    ) {
                                        item.content.forEachIndexed { itemIndex, function ->
                                            function(
                                                ListItemDefaults.segmentedShapes(
                                                    index = itemIndex,
                                                    count = item.content.size
                                                )
                                            )
                                        }
                                    }
                                }

                                is ModelOptionsType.Item -> item.content(
                                    ListItemDefaults.shapes(
                                        shape = MaterialTheme.shapes.large
                                    )
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun rememberModelOptionsScope(
    content: ModelOptionsScope.() -> Unit = {},
): ModelOptionsScope = ModelOptionsScopeImpl(content)