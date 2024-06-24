@file:Suppress("INLINE_FROM_HIGHER_PLATFORM")

package com.programmersbox.common.creator

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.basicMarquee
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.programmersbox.common.*
import com.programmersbox.common.components.LoadingImage
import com.programmersbox.common.home.modelItems
import com.programmersbox.common.paging.collectAsLazyPagingItems
import dev.chrisbanes.haze.HazeState
import dev.chrisbanes.haze.haze
import dev.chrisbanes.haze.hazeChild

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun CivitAiUserScreen(
    network: Network = LocalNetwork.current,
    username: String,
) {
    val hazeState = remember { HazeState() }
    val dataStore = LocalDataStore.current
    val showNsfw by remember { dataStore.showNsfw.flow }.collectAsStateWithLifecycle(false)
    val blurStrength by remember { dataStore.hideNsfwStrength.flow }.collectAsStateWithLifecycle(6f)
    val database = LocalDatabase.current
    val favorites by database.getFavorites().collectAsStateWithLifecycle(emptyList())
    val blacklisted by database.getBlacklistedItems().collectAsStateWithLifecycle(emptyList())
    val viewModel = viewModel { CivitAiUserViewModel(network, dataStore, database, username) }
    val showBlur by dataStore.rememberShowBlur()
    val navController = LocalNavController.current

    val lazyPagingItems = viewModel.pager.collectAsLazyPagingItems()

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        viewModel.username,
                        modifier = Modifier.basicMarquee()
                    )
                },
                navigationIcon = {
                    IconButton(
                        onClick = { navController.popBackStack() }
                    ) { Icon(Icons.AutoMirrored.Filled.ArrowBack, null) }
                },
                actions = {
                    if (lazyPagingItems.itemSnapshotList.isNotEmpty()) {
                        lazyPagingItems[0]?.creator?.let { creator ->
                            IconButton(
                                onClick = {
                                    if (favorites.any { it.name == viewModel.username }) {
                                        viewModel.removeToFavorites(creator)
                                    } else {
                                        viewModel.addToFavorites(creator)
                                    }
                                }
                            ) {
                                Icon(
                                    if (favorites.any { it.name == viewModel.username })
                                        Icons.Default.Favorite
                                    else
                                        Icons.Default.FavoriteBorder,
                                    null
                                )
                            }
                            LoadingImage(
                                creator.image.orEmpty(),
                                modifier = Modifier
                                    .size(48.dp)
                                    .clip(CircleShape)
                            )
                        }
                    }
                },
                colors = if (showBlur) TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
                else TopAppBarDefaults.topAppBarColors(),
                modifier = Modifier.ifTrue(showBlur) { hazeChild(hazeState) }
            )
        },
    ) { padding ->
        LazyVerticalGrid(
            columns = adaptiveGridCell(),
            verticalArrangement = Arrangement.spacedBy(4.dp),
            horizontalArrangement = Arrangement.spacedBy(4.dp),
            contentPadding = padding,
            modifier = Modifier
                .ifTrue(showBlur) { haze(state = hazeState) }
                .fillMaxSize()
        ) {
            item(
                span = { GridItemSpan(maxLineSpan) }
            ) {
                CenterAlignedTopAppBar(
                    title = { Text("Models made by $username") },
                    windowInsets = WindowInsets(0.dp),
                )
            }
            modelItems(
                lazyPagingItems = lazyPagingItems,
                navController = navController,
                showNsfw = showNsfw,
                blurStrength = blurStrength,
                database = favorites,
                blacklisted = blacklisted,
            )
        }
    }
}