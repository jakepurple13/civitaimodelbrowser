package com.programmersbox.common

import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.Modifier
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.viewmodel.navigation3.rememberViewModelStoreNavEntryDecorator
import androidx.navigation.NavController
import androidx.navigation.NavOptions
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberSaveableStateHolderNavEntryDecorator
import androidx.navigation3.ui.NavDisplay
import androidx.room.RoomDatabase
import com.programmersbox.common.blacklisted.BlacklistedScreen
import com.programmersbox.common.creator.CivitAiUserScreen
import com.programmersbox.common.db.AppDatabase
import com.programmersbox.common.db.CivitDb
import com.programmersbox.common.db.FavoritesDao
import com.programmersbox.common.db.FavoritesUI
import com.programmersbox.common.db.getRoomDatabase
import com.programmersbox.common.details.CivitAiDetailScreen
import com.programmersbox.common.details.CivitAiModelImagesScreen
import com.programmersbox.common.home.CivitAiScreen
import dev.chrisbanes.haze.LocalHazeStyle
import dev.chrisbanes.haze.materials.HazeMaterials
import kotlinx.serialization.Serializable

@Composable
internal fun App(
    onShareClick: (String) -> Unit,
    producePath: () -> String,
    onExport: (CivitDb) -> Unit = {},
    onImport: () -> String = { "" },
    export: @Composable () -> Unit = {},
    import: (@Composable () -> Unit)? = null,
    builder: RoomDatabase.Builder<AppDatabase>,
) {
    val viewModel = viewModel { AppViewModel(DataStore.getStore(producePath)) }
    CompositionLocalProvider(
        LocalDataStore provides viewModel.dataStore,
        LocalHazeStyle provides HazeMaterials.regular(),
        LocalDatabaseDao provides remember { getRoomDatabase(builder).getDao() }
    ) {
        Surface {
            val backStack = remember { mutableStateListOf<NavKey>(Screen.List) }
            NavDisplay(
                backStack = backStack,
                entryDecorators = listOf(
                    rememberSaveableStateHolderNavEntryDecorator(),
                    rememberViewModelStoreNavEntryDecorator()
                ),
                entryProvider = entryProvider {
                    entry<Screen.List> {
                        CivitAiScreen(
                            onNavigateToDetail = { id -> backStack.add(Screen.Detail(id)) },
                            onNavigateToFavorites = { backStack.add(Screen.Favorites) },
                            onNavigateToSettings = { backStack.add(Screen.Settings) }
                        )
                    }
                    entry<Screen.Detail> {
                        CivitAiDetailScreen(
                            id = it.modelId,
                            onShareClick = onShareClick,
                            onNavigateToUser = { username -> backStack.add(Screen.User(username)) },
                            onNavigateToDetailImages = { id, name ->
                                backStack.add(
                                    Screen.DetailsImage(
                                        id.toString(),
                                        name
                                    )
                                )
                            }
                        )
                    }
                    entry<Screen.DetailsImage> {
                        CivitAiModelImagesScreen(
                            modelId = it.modelId,
                            modelName = it.modelName
                        )
                    }
                    entry<Screen.User> {
                        CivitAiUserScreen(
                            username = it.username,
                            onNavigateToDetail = { id -> backStack.add(Screen.Detail(id)) }
                        )
                    }
                    entry<Screen.Favorites> {
                        FavoritesUI(
                            onNavigateToDetail = { id -> backStack.add(Screen.Detail(id.toString())) },
                            onNavigateToUser = { username -> backStack.add(Screen.User(username)) }
                        )
                    }
                    entry<Screen.Settings> {
                        SettingsScreen(
                            onExport = onExport,
                            onImport = onImport,
                            export = export,
                            import = import,
                            onNavigateToBlacklisted = { backStack.add(Screen.Settings.Blacklisted) }
                        )
                    }
                    entry<Screen.Settings.Blacklisted> { BlacklistedScreen() }
                    entry<Screen.Settings.Screen> {
                        SettingsScreen(
                            onExport = onExport,
                            onImport = onImport,
                            export = export,
                            import = import,
                            onNavigateToBlacklisted = { backStack.add(Screen.Settings.Blacklisted) }
                        )
                    }
                },
                transitionSpec = {
                    // Slide in from right when navigating forward
                    slideInHorizontally(initialOffsetX = { it }) togetherWith
                            slideOutHorizontally(targetOffsetX = { -it })
                },
                popTransitionSpec = {
                    // Slide in from left when navigating back
                    slideInHorizontally(initialOffsetX = { -it }) togetherWith
                            slideOutHorizontally(targetOffsetX = { it })
                },
                predictivePopTransitionSpec = {
                    // Slide in from left when navigating back
                    slideInHorizontally(initialOffsetX = { -it }) togetherWith
                            slideOutHorizontally(targetOffsetX = { it })
                },
                modifier = Modifier.fillMaxSize()
            )
        }
    }
}

class AppViewModel(val dataStore: DataStore) : ViewModel()

internal val LocalNavController = staticCompositionLocalOf<NavController> { error("Nope") }
internal val LocalDataStore = staticCompositionLocalOf<DataStore> { error("Nope") }
val LocalDatabaseDao = staticCompositionLocalOf<FavoritesDao> { error("Nothing") }
internal val LocalNetwork = staticCompositionLocalOf { Network() }

fun NavController.navigateToDetail(id: Long) {
    navigate(
        route = Screen.Detail(id.toString()),
        navOptions = NavOptions.Builder()
            .setLaunchSingleTop(true)
            .build()
    )
}

fun NavController.navigateToDetailImages(id: Long, name: String) {
    navigate(
        route = Screen.DetailsImage(id.toString(), name),
        navOptions = NavOptions.Builder()
            .setLaunchSingleTop(true)
            .build()
    )
}

fun NavController.navigateToUser(username: String) {
    navigate(
        route = Screen.User(username),
        navOptions = NavOptions.Builder()
            .setLaunchSingleTop(true)
            .build()
    )
}

sealed class Screen {
    @Serializable
    data object List : NavKey

    @Serializable
    class Detail(val modelId: String) : NavKey

    @Serializable
    data object Settings : NavKey {
        @Serializable
        data object Screen : NavKey

        @Serializable
        data object Blacklisted : NavKey
    }

    @Serializable
    data object Favorites : NavKey

    @Serializable
    class User(val username: String) : NavKey

    @Serializable
    class DetailsImage(val modelId: String, val modelName: String) : NavKey
}