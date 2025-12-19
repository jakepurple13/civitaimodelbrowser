package com.programmersbox.common.home

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateIntAsState
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.text.input.TextFieldState
import androidx.compose.foundation.text.input.clearText
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material3.AppBarWithSearch
import androidx.compose.material3.ExpandedFullScreenSearchBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SearchBarDefaults
import androidx.compose.material3.SearchBarValue
import androidx.compose.material3.Text
import androidx.compose.material3.rememberSearchBarState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.paging.compose.collectAsLazyPagingItems
import com.programmersbox.common.BackButton
import com.programmersbox.common.DataStore
import com.programmersbox.common.NetworkConnectionRepository
import com.programmersbox.common.adaptiveGridCell
import com.programmersbox.common.db.FavoritesDao
import com.programmersbox.common.ifTrue
import dev.chrisbanes.haze.HazeProgressive
import dev.chrisbanes.haze.HazeState
import dev.chrisbanes.haze.LocalHazeStyle
import dev.chrisbanes.haze.hazeEffect
import dev.chrisbanes.haze.hazeSource
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
    val hazeState = remember { HazeState() }
    val db = koinInject<FavoritesDao>()
    val dataStore = koinInject<DataStore>()
    val database by db
        .getFavoriteModels()
        .collectAsStateWithLifecycle(emptyList())

    val blacklisted by db
        .getBlacklisted()
        .collectAsStateWithLifecycle(emptyList())
    val showBlur by dataStore.rememberShowBlur()
    val showNsfw by dataStore.showNsfw()
    val blurStrength by dataStore.hideNsfwStrength()
    val hazeStyle = LocalHazeStyle.current

    Scaffold(
        topBar = {
            SearchAppBar(
                searchQuery = viewModel.searchQuery,
                onSearch = viewModel::onSearch,
                showBlur = showBlur,
                searchCount = lazyPagingItems.itemCount,
                modifier = Modifier.ifTrue(showBlur) {
                    hazeEffect(hazeState) {
                        progressive = HazeProgressive.verticalGradient(
                            startIntensity = 1f,
                            endIntensity = 0f,
                            preferPerformance = true
                        )
                        style = hazeStyle
                    }
                }
            )
        }
    ) { paddingValues ->
        LazyVerticalGrid(
            columns = adaptiveGridCell(),
            verticalArrangement = Arrangement.spacedBy(4.dp),
            horizontalArrangement = Arrangement.spacedBy(4.dp),
            contentPadding = paddingValues,
            modifier = Modifier
                .ifTrue(showBlur) { hazeSource(state = hazeState) }
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
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SearchAppBar(
    searchQuery: TextFieldState,
    onSearch: (String) -> Unit,
    searchCount: Int,
    showBlur: Boolean,
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
        //TODO: This will be previous searches
    }
}