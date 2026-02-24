package com.programmersbox.common.presentation.home

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateIntAsState
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.input.TextFieldState
import androidx.compose.foundation.text.input.clearText
import androidx.compose.foundation.text.input.setTextAndPlaceCursorAtEnd
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.AppBarWithSearch
import androidx.compose.material3.ExpandedFullScreenSearchBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SearchBarDefaults
import androidx.compose.material3.SearchBarValue
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberSearchBarState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.paging.LoadState
import androidx.paging.compose.collectAsLazyPagingItems
import com.programmersbox.common.BackButton
import com.programmersbox.common.DataStore
import com.programmersbox.common.Models
import com.programmersbox.common.NetworkConnectionRepository
import com.programmersbox.common.WindowedScaffold
import com.programmersbox.common.adaptiveGridCell
import com.programmersbox.common.db.FavoritesDao
import com.programmersbox.common.db.SearchHistoryItem
import com.programmersbox.common.presentation.components.CivitRail
import dev.chrisbanes.haze.HazeProgressive
import dev.chrisbanes.haze.LocalHazeStyle
import dev.chrisbanes.haze.hazeEffect
import dev.chrisbanes.haze.hazeSource
import dev.chrisbanes.haze.rememberHazeState
import kotlinx.collections.immutable.PersistentList
import kotlinx.collections.immutable.persistentListOf
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.launch
import org.koin.compose.koinInject
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun SearchScreen(
    onNavigateToDetail: (String) -> Unit,
    onNavigateToUser: (String) -> Unit,
    onNavigateToDetailImages: (Long, String) -> Unit,
    viewModel: CivitAiSearchViewModel = koinViewModel(),
) {
    val lazyPagingItems = viewModel.pager.collectAsLazyPagingItems()
    val connectionRepository = koinInject<NetworkConnectionRepository>()
    val shouldShowMedia by remember { derivedStateOf { connectionRepository.shouldShowMedia } }
    val db = koinInject<FavoritesDao>()
    val dataStore = koinInject<DataStore>()
    val showFavorites by dataStore.rememberShowFavorites()
    val database by remember {
        derivedStateOf {
            if (showFavorites) db.getFavoriteModels()
            else flowOf(emptyList())
        }
    }
        .value
        .collectAsStateWithLifecycle(emptyList())

    val blacklisted by db
        .getBlacklisted()
        .collectAsStateWithLifecycle(emptyList())

    val useNewCardLook by dataStore.rememberUseNewCardLook()
    val showBlur by dataStore.rememberShowBlur()
    val hazeState = rememberHazeState(showBlur)
    val useProgressive by dataStore.rememberUseProgressive()
    val showNsfw by dataStore.showNsfw()
    val blurStrength by dataStore.hideNsfwStrength()
    val hazeStyle = LocalHazeStyle.current
    val scope = rememberCoroutineScope()

    val doubleClickBehavior by dataStore.rememberDoubleClickBehavior()
    val doubleClickBehaviorAction: ((Models) -> Unit)? by remember {
        derivedStateOf {
            createDoubleClickBehaviorAction(
                doubleClickBehavior = doubleClickBehavior,
                blacklisted = blacklisted,
                db = db,
                scope = scope,
            )
        }
    }

    val searchList by viewModel
        .searchFlow
        .collectAsStateWithLifecycle(persistentListOf())

    WindowedScaffold(
        topBar = {
            SearchAppBar(
                searchQuery = viewModel.searchQuery,
                onSearch = viewModel::onSearch,
                showBlur = showBlur,
                searchCount = lazyPagingItems.itemCount,
                searchHistory = searchList,
                onRemoveSearchHistory = viewModel::removeSearchHistory,
                modifier = Modifier.hazeEffect(hazeState) {
                    progressive = if (useProgressive)
                        HazeProgressive.verticalGradient(
                            startIntensity = 1f,
                            endIntensity = 0f,
                            preferPerformance = true
                        )
                    else
                        null
                    style = hazeStyle
                }
            )
        },
        rail = { CivitRail() },
    ) { paddingValues ->
        LazyVerticalGrid(
            columns = adaptiveGridCell(),
            verticalArrangement = Arrangement.spacedBy(4.dp),
            horizontalArrangement = Arrangement.spacedBy(4.dp),
            contentPadding = paddingValues,
            modifier = Modifier
                .padding(vertical = 4.dp)
                .hazeSource(state = hazeState)
                .fillMaxSize()
        ) {
            modelItems(
                lazyPagingItems = lazyPagingItems,
                onNavigateToDetail = onNavigateToDetail,
                showNsfw = showNsfw,
                blurStrength = blurStrength,
                database = database,
                blacklisted = blacklisted,
                shouldShowMedia = shouldShowMedia,
                onNavigateToUser = onNavigateToUser,
                onNavigateToDetailImages = onNavigateToDetailImages,
                onDoubleClick = doubleClickBehaviorAction,
                useNewCardLook = useNewCardLook,
            )
            if (
                lazyPagingItems.itemCount == 0 &&
                !lazyPagingItems.loadState.hasType<LoadState.Loading>() &&
                !lazyPagingItems.loadState.hasType<LoadState.Error>()
            ) {
                item(
                    span = { GridItemSpan(maxLineSpan) }
                ) {
                    Column(
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.fillMaxSize()
                    ) {
                        Icon(
                            Icons.Default.Search,
                            null,
                            modifier = Modifier.size(96.dp)
                        )
                        Text("Start Searching!")
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
private fun SearchAppBar(
    searchQuery: TextFieldState,
    onSearch: (String) -> Unit,
    searchCount: Int,
    showBlur: Boolean,
    searchHistory: PersistentList<SearchHistoryItem>,
    onRemoveSearchHistory: (SearchHistoryItem) -> Unit,
    modifier: Modifier = Modifier,
) {
    val searchBarState = rememberSearchBarState()

    val scope = rememberCoroutineScope()
    val appBarWithSearchColors = SearchBarDefaults.appBarWithSearchColors(
        searchBarColors = SearchBarDefaults.colors(
            containerColor = if (showBlur)
                Color.Transparent
            else
                MaterialTheme.colorScheme.surfaceContainerHigh
        ),
        appBarContainerColor = if (showBlur)
            Color.Transparent
        else
            MaterialTheme.colorScheme.surface
    )

    val inputField = @Composable {
        SearchBarDefaults.InputField(
            searchBarState = searchBarState,
            textFieldState = searchQuery,
            onSearch = { query ->
                scope.launch { searchBarState.animateToCollapsed() }
                    .invokeOnCompletion { onSearch(query) }
            },
            placeholder = {
                Text(
                    modifier = Modifier.fillMaxWidth(),
                    text = "Search CivitAi",
                    textAlign = TextAlign.Center,
                )
            },
            leadingIcon = {
                AnimatedContent(
                    searchBarState.currentValue == SearchBarValue.Expanded,
                    transitionSpec = {
                        slideInHorizontally { -it } + fadeIn() togetherWith
                                slideOutHorizontally { -it } + fadeOut()
                    },
                    contentAlignment = Alignment.Center
                ) { target ->
                    if (target) {
                        IconButton(
                            onClick = { scope.launch { searchBarState.animateToCollapsed() } }
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = null
                            )
                        }
                    }
                }
            },
            trailingIcon = {
                AnimatedVisibility(
                    searchQuery.text.isNotEmpty(),
                    enter = slideInHorizontally { it } + fadeIn(),
                    exit = slideOutHorizontally { it } + fadeOut()
                ) {
                    IconButton(
                        onClick = {
                            searchQuery.clearText()
                            onSearch("")
                        }
                    ) { Icon(Icons.Default.Clear, null) }
                }
            },
        )
    }

    AppBarWithSearch(
        state = searchBarState,
        inputField = inputField,
        navigationIcon = { BackButton() },
        actions = { Text("(${animateIntAsState(searchCount).value})") },
        colors = appBarWithSearchColors,
        modifier = modifier
    )

    ExpandedFullScreenSearchBar(
        state = searchBarState,
        inputField = inputField,
    ) {
        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            items(searchHistory) { item ->
                var showDialog by remember { mutableStateOf(false) }

                if (showDialog) {
                    AlertDialog(
                        onDismissRequest = { showDialog = false },
                        title = { Text("Remove from History?") },
                        text = { Text("Remove this search from history?") },
                        confirmButton = {
                            TextButton(
                                onClick = {
                                    scope.launch {
                                        onRemoveSearchHistory(item)
                                        showDialog = false
                                    }
                                }
                            ) { Text("Confirm") }
                        },
                        dismissButton = {
                            TextButton(
                                onClick = { showDialog = false }
                            ) { Text("Dismiss") }
                        }
                    )
                }

                ListItem(
                    content = { Text(item.searchQuery) },
                    leadingContent = { Icon(Icons.Default.History, null) },
                    trailingContent = {
                        IconButton(
                            onClick = { showDialog = true }
                        ) { Icon(Icons.Default.Clear, null) }
                    },
                    onClick = {
                        scope.launch {
                            searchQuery.setTextAndPlaceCursorAtEnd(item.searchQuery)
                            searchBarState.animateToCollapsed()
                        }.invokeOnCompletion { onSearch(item.searchQuery) }
                    },
                    modifier = Modifier.animateItem()
                )
            }
        }
    }
}