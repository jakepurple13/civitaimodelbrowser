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
import androidx.lifecycle.viewmodel.navigation3.rememberViewModelStoreNavEntryDecorator
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.rememberSaveableStateHolderNavEntryDecorator
import androidx.navigation3.ui.NavDisplay
import androidx.room.RoomDatabase
import com.programmersbox.common.blacklisted.BlacklistedScreen
import com.programmersbox.common.creator.CivitAiUserScreen
import com.programmersbox.common.creator.CivitAiUserViewModel
import com.programmersbox.common.db.AppDatabase
import com.programmersbox.common.db.CivitDb
import com.programmersbox.common.db.FavoritesDao
import com.programmersbox.common.db.FavoritesUI
import com.programmersbox.common.db.FavoritesViewModel
import com.programmersbox.common.db.getRoomDatabase
import com.programmersbox.common.details.CivitAiDetailScreen
import com.programmersbox.common.details.CivitAiDetailViewModel
import com.programmersbox.common.details.CivitAiModelImagesScreen
import com.programmersbox.common.details.CivitAiModelImagesViewModel
import com.programmersbox.common.home.CivitAiScreen
import com.programmersbox.common.home.CivitAiSearchViewModel
import com.programmersbox.common.home.CivitAiViewModel
import dev.chrisbanes.haze.LocalHazeStyle
import dev.chrisbanes.haze.materials.HazeMaterials
import kotlinx.serialization.Serializable
import org.koin.compose.KoinApplication
import org.koin.compose.koinInject
import org.koin.compose.navigation3.koinEntryProvider
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.module.dsl.singleOf
import org.koin.core.module.dsl.viewModel
import org.koin.core.module.dsl.viewModelOf
import org.koin.core.parameter.parametersOf
import org.koin.dsl.module
import org.koin.dsl.navigation3.navigation

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
    val backStack = remember { mutableStateListOf<NavKey>(Screen.List) }
    KoinApplication(
        application = {
            modules(
                module {
                    singleOf(::Network)
                    single { DataStore.getStore(producePath) }
                    single { getRoomDatabase(builder).getDao() }
                    viewModelOf(::CivitAiViewModel)
                    viewModelOf(::CivitAiSearchViewModel)
                    viewModel { CivitAiDetailViewModel(get(), it.get(), get()) }
                    viewModel {
                        CivitAiModelImagesViewModel(
                            modelId = it.get(),
                            dataStore = get(),
                            network = get(),
                            database = get()
                        )
                    }
                    viewModel {
                        CivitAiUserViewModel(
                            network = get(),
                            dataStore = get(),
                            database = get(),
                            username = it.get()
                        )
                    }
                    viewModelOf(::FavoritesViewModel)

                    navigation<Screen.List> {
                        CivitAiScreen(
                            onNavigateToDetail = { id -> backStack.add(Screen.Detail(id)) },
                            onNavigateToFavorites = { backStack.add(Screen.Favorites) },
                            onNavigateToSettings = { backStack.add(Screen.Settings) }
                        )
                    }

                    navigation<Screen.Detail> {
                        CivitAiDetailScreen(
                            id = it.modelId,
                            viewModel = koinViewModel { parametersOf(it.modelId) },
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

                    navigation<Screen.DetailsImage> {
                        CivitAiModelImagesScreen(
                            modelName = it.modelName,
                            viewModel = koinViewModel { parametersOf(it.modelId) }
                        )
                    }

                    navigation<Screen.User> {
                        CivitAiUserScreen(
                            viewModel = koinViewModel { parametersOf(it.username) },
                            username = it.username,
                            onNavigateToDetail = { id -> backStack.add(Screen.Detail(id)) }
                        )
                    }
                    navigation<Screen.Favorites> {
                        FavoritesUI(
                            viewModel = koinViewModel(),
                            onNavigateToDetail = { id -> backStack.add(Screen.Detail(id.toString())) },
                            onNavigateToUser = { username -> backStack.add(Screen.User(username)) }
                        )
                    }
                    navigation<Screen.Settings> {
                        SettingsScreen(
                            onExport = onExport,
                            onImport = onImport,
                            export = export,
                            import = import,
                            onNavigateToBlacklisted = { backStack.add(Screen.Settings.Blacklisted) }
                        )
                    }
                    navigation<Screen.Settings.Blacklisted> { BlacklistedScreen() }
                    navigation<Screen.Settings.Screen> {
                        SettingsScreen(
                            onExport = onExport,
                            onImport = onImport,
                            export = export,
                            import = import,
                            onNavigateToBlacklisted = { backStack.add(Screen.Settings.Blacklisted) }
                        )
                    }
                }
            )
        }
    ) {
        CompositionLocalProvider(
            LocalHazeStyle provides HazeMaterials.regular(),
            LocalDatabaseDao provides koinInject()
        ) {
            Surface {
                NavDisplay(
                    backStack = backStack,
                    entryDecorators = listOf(
                        rememberSaveableStateHolderNavEntryDecorator(),
                        rememberViewModelStoreNavEntryDecorator()
                    ),
                    entryProvider = koinEntryProvider(),
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
}

val LocalDatabaseDao = staticCompositionLocalOf<FavoritesDao> { error("Nothing") }

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