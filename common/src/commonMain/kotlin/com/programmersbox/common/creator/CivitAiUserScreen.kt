package com.programmersbox.common.creator

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.basicMarquee
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.PlaylistAdd
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.SearchOff
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.AppBarRow
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.paging.compose.collectAsLazyPagingItems
import com.dokar.sonner.ToastType
import com.dokar.sonner.ToasterState
import com.programmersbox.common.BackButton
import com.programmersbox.common.DataStore
import com.programmersbox.common.NetworkConnectionRepository
import com.programmersbox.common.adaptiveGridCell
import com.programmersbox.common.components.ListChoiceScreen
import com.programmersbox.common.components.LoadingImage
import com.programmersbox.common.db.FavoriteType
import com.programmersbox.common.db.FavoritesDao
import com.programmersbox.common.db.ListDao
import com.programmersbox.common.home.modelItems
import com.programmersbox.common.ifTrue
import com.programmersbox.common.qrcode.QrCodeType
import com.programmersbox.common.qrcode.ShareViaQrCode
import dev.chrisbanes.haze.HazeProgressive
import dev.chrisbanes.haze.HazeState
import dev.chrisbanes.haze.LocalHazeStyle
import dev.chrisbanes.haze.hazeEffect
import dev.chrisbanes.haze.hazeSource
import kotlinx.coroutines.launch
import org.koin.compose.koinInject
import org.koin.compose.viewmodel.koinViewModel

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun CivitAiUserScreen(
    username: String,
    viewModel: CivitAiUserViewModel = koinViewModel(),
    onNavigateToDetail: (String) -> Unit,
) {
    val connectionRepository = koinInject<NetworkConnectionRepository>()
    val shouldShowMedia by remember { derivedStateOf { connectionRepository.shouldShowMedia } }
    val hazeState = remember { HazeState() }
    val dataStore = koinInject<DataStore>()
    val showNsfw by remember { dataStore.showNsfw.flow }.collectAsStateWithLifecycle(false)
    val blurStrength by remember { dataStore.hideNsfwStrength.flow }.collectAsStateWithLifecycle(6f)
    val database = koinInject<FavoritesDao>()
    val favorites by database.getFavoriteModels().collectAsStateWithLifecycle(emptyList())
    val blacklisted by database.getBlacklisted().collectAsStateWithLifecycle(emptyList())
    val showBlur by dataStore.rememberShowBlur()

    val lazyPagingItems = viewModel.pager.collectAsLazyPagingItems()

    val hazeStyle = LocalHazeStyle.current

    var showQrCode by remember { mutableStateOf(false) }

    if (showQrCode) {
        ShareViaQrCode(
            title = username,
            url = username,
            qrCodeType = QrCodeType.User,
            id = username,
            username = username,
            imageUrl = "",
            onClose = { showQrCode = false }
        )
    }

    val scope = rememberCoroutineScope()
    var showLists by remember { mutableStateOf(false) }
    val listState = rememberModalBottomSheetState(true)

    if (showLists) {
        val listDao = koinInject<ListDao>()
        lazyPagingItems[0]?.creator?.let { creator ->
            ModalBottomSheet(
                onDismissRequest = { showLists = false },
                containerColor = MaterialTheme.colorScheme.surface,
                sheetState = listState
            ) {
                val toaster = koinInject<ToasterState>()
                ListChoiceScreen(
                    username = username,
                    onClick = { item ->
                        scope.launch {
                            listDao.addToList(
                                uuid = item.item.uuid,
                                id = 0,
                                name = creator.username.orEmpty(),
                                description = null,
                                type = "Creator",
                                nsfw = false,
                                imageUrl = creator.image,
                                favoriteType = FavoriteType.Creator,
                                hash = null
                            )
                            toaster.show("Added to List", type = ToastType.Success)
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
    }

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
                            AppBarRow(
                                maxItemCount = 2
                            ) {
                                clickableItem(
                                    onClick = {
                                        if (favorites.any { it.name == viewModel.username }) {
                                            viewModel.removeToFavorites(creator)
                                        } else {
                                            viewModel.addToFavorites(creator)
                                        }
                                    },
                                    icon = {
                                        Icon(
                                            if (favorites.any { it.name == viewModel.username })
                                                Icons.Default.Favorite
                                            else
                                                Icons.Default.FavoriteBorder,
                                            null
                                        )
                                    },
                                    label = "Favorite"
                                )

                                clickableItem(
                                    onClick = { showLists = true },
                                    icon = { Icon(Icons.AutoMirrored.Filled.PlaylistAdd, null) },
                                    label = "Lists"
                                )

                                clickableItem(
                                    onClick = { showQrCode = true },
                                    icon = { Icon(Icons.Default.Share, null) },
                                    label = "Share"
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
                span = { GridItemSpan(maxLineSpan) },
                contentType = "header"
            ) {
                CenterAlignedTopAppBar(
                    title = { Text("Models made by $username") },
                    windowInsets = WindowInsets(0.dp),
                )
            }
            if (lazyPagingItems.itemCount == 0) {
                item(
                    span = { GridItemSpan(maxLineSpan) },
                    contentType = "no models"
                ) {
                    Column(
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.fillMaxSize()
                    ) {
                        Icon(
                            Icons.Default.SearchOff,
                            null,
                            modifier = Modifier.size(96.dp)
                        )
                        Text("No Models Found")
                    }
                }
            } else {
                modelItems(
                    lazyPagingItems = lazyPagingItems,
                    onNavigateToDetail = onNavigateToDetail,
                    showNsfw = showNsfw,
                    blurStrength = blurStrength,
                    database = favorites,
                    blacklisted = blacklisted,
                    shouldShowMedia = shouldShowMedia,
                )
            }
        }
    }
}