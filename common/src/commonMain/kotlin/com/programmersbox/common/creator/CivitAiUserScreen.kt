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
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.unit.dp
import com.programmersbox.common.*
import com.programmersbox.common.components.LoadingImage
import com.programmersbox.common.home.modelItems
import com.programmersbox.common.paging.collectAsLazyPagingItems
import moe.tlaster.precompose.flow.collectAsStateWithLifecycle
import moe.tlaster.precompose.viewmodel.viewModel

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun CivitAiUserScreen(
    network: Network = LocalNetwork.current,
    username: String,
) {
    val database by LocalDatabase.current.getFavorites().collectAsStateWithLifecycle(emptyList())
    val dataStore = LocalDataStore.current
    val showNsfw by remember { dataStore.showNsfw.flow }.collectAsStateWithLifecycle(false)
    val blurStrength by remember { dataStore.hideNsfwStrength.flow }.collectAsStateWithLifecycle(6f)
    val viewModel = viewModel { CivitAiUserViewModel(network, dataStore, username) }
    val scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior()
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
                    ) { Icon(Icons.Default.ArrowBack, null) }
                },
                actions = {
                    if (lazyPagingItems.itemSnapshotList.isNotEmpty()) {
                        lazyPagingItems[0]?.creator?.let { creator ->
                            LoadingImage(
                                creator.image.orEmpty(),
                                modifier = Modifier
                                    .size(48.dp)
                                    .clip(CircleShape)
                            )
                        }
                    }
                },
                scrollBehavior = scrollBehavior
            )
        },
        modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection)
    ) { padding ->
        LazyVerticalGrid(
            columns = adaptiveGridCell(),
            verticalArrangement = Arrangement.spacedBy(4.dp),
            horizontalArrangement = Arrangement.spacedBy(4.dp),
            contentPadding = padding,
            modifier = Modifier.fillMaxSize()
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
                database = database
            )
        }
    }
}