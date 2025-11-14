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
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.programmersbox.common.BackButton
import com.programmersbox.common.LocalDataStore
import com.programmersbox.common.LocalDatabaseDao
import com.programmersbox.common.LocalNetwork
import com.programmersbox.common.Network
import com.programmersbox.common.adaptiveGridCell
import com.programmersbox.common.components.LoadingImage
import com.programmersbox.common.home.modelItems
import com.programmersbox.common.ifTrue
import com.programmersbox.common.paging.collectAsLazyPagingItems
import dev.chrisbanes.haze.HazeProgressive
import dev.chrisbanes.haze.HazeState
import dev.chrisbanes.haze.LocalHazeStyle
import dev.chrisbanes.haze.hazeEffect
import dev.chrisbanes.haze.hazeSource

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun CivitAiUserScreen(
    network: Network = LocalNetwork.current,
    username: String,
    onNavigateToDetail: (String) -> Unit,
) {
    val hazeState = remember { HazeState() }
    val dataStore = LocalDataStore.current
    val showNsfw by remember { dataStore.showNsfw.flow }.collectAsStateWithLifecycle(false)
    val blurStrength by remember { dataStore.hideNsfwStrength.flow }.collectAsStateWithLifecycle(6f)
    val database = LocalDatabaseDao.current
    val favorites by database.getFavoriteModels().collectAsStateWithLifecycle(emptyList())
    val blacklisted by database.getBlacklisted().collectAsStateWithLifecycle(emptyList())
    val viewModel = viewModel { CivitAiUserViewModel(network, dataStore, database, username) }
    val showBlur by dataStore.rememberShowBlur()

    val lazyPagingItems = viewModel.pager.collectAsLazyPagingItems()

    val hazeStyle = LocalHazeStyle.current

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        viewModel.username,
                        modifier = Modifier.basicMarquee()
                    )
                },
                navigationIcon = { BackButton() },
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
                modifier = Modifier.ifTrue(showBlur) {
                    hazeEffect(hazeState, hazeStyle) {
                        progressive = HazeProgressive.verticalGradient(
                            startIntensity = 1f,
                            endIntensity = 0f,
                            preferPerformance = true
                        )
                    }
                }
            )
        },
    ) { padding ->
        LazyVerticalGrid(
            columns = adaptiveGridCell(),
            verticalArrangement = Arrangement.spacedBy(4.dp),
            horizontalArrangement = Arrangement.spacedBy(4.dp),
            contentPadding = padding,
            modifier = Modifier
                .ifTrue(showBlur) { hazeSource(state = hazeState) }
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
                onNavigateToDetail = onNavigateToDetail,
                showNsfw = showNsfw,
                blurStrength = blurStrength,
                database = favorites,
                blacklisted = blacklisted,
            )
        }
    }
}