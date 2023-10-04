package com.programmersbox.common.db

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.programmersbox.common.*
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
                ModelItem(
                    models = model,
                    onClick = {
                        navController.navigate(Screen.Detail.routeId.replace("{modelId}", model.id.toString()))
                    },
                    showNsfw = showNsfw,
                    blurStrength = blurStrength.dp,
                    modifier = Modifier.animateItemPlacement()
                )
            }
        }
    }
}

@Composable
private fun ModelItem(
    models: Models,
    showNsfw: Boolean,
    blurStrength: Dp,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val imageModel = remember { models.modelVersions.firstOrNull()?.images?.firstOrNull() }
    CoverCard(
        imageUrl = remember { imageModel?.url.orEmpty() },
        name = models.name,
        type = models.type,
        isNsfw = models.nsfw || imageModel?.nsfw?.canNotShow() == true,
        showNsfw = showNsfw,
        blurStrength = blurStrength,
        onClick = onClick,
        modifier = modifier.size(
            width = ComposableUtils.IMAGE_WIDTH,
            height = ComposableUtils.IMAGE_HEIGHT
        )
    )
}

@OptIn(ExperimentalLayoutApi::class)
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