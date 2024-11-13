package com.programmersbox.common

import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.remember
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.NavOptions
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.navigation
import androidx.navigation.compose.rememberNavController
import androidx.navigation.toRoute
import androidx.room.RoomDatabase
import com.programmersbox.common.blacklisted.BlacklistedScreen
import com.programmersbox.common.creator.CivitAiUserScreen
import com.programmersbox.common.db.*
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
    val navController = rememberNavController()
    val viewModel = viewModel { AppViewModel(DataStore.getStore(producePath)) }
    CompositionLocalProvider(
        LocalNavController provides navController,
        LocalDataStore provides viewModel.dataStore,
        LocalHazeStyle provides HazeMaterials.regular(),
        LocalDatabaseDao provides remember { getRoomDatabase(builder).getDao() }
    ) {
        Surface {
            NavHost(
                navController = navController,
                startDestination = Screen.List,
                enterTransition = { slideIntoContainer(AnimatedContentTransitionScope.SlideDirection.Start) },
                exitTransition = { slideOutOfContainer(AnimatedContentTransitionScope.SlideDirection.End) },
            ) {
                composable<Screen.List>(
                    enterTransition = { slideIntoContainer(AnimatedContentTransitionScope.SlideDirection.End) },
                    exitTransition = { slideOutOfContainer(AnimatedContentTransitionScope.SlideDirection.Start) }
                ) { CivitAiScreen() }

                composable<Screen.Detail> {
                    CivitAiDetailScreen(
                        id = it.toRoute<Screen.Detail>().modelId,
                        onShareClick = onShareClick
                    )
                }

                composable<Screen.DetailsImage> {
                    val details = it.toRoute<Screen.DetailsImage>()
                    CivitAiModelImagesScreen(
                        modelId = details.modelId,
                        modelName = details.modelName
                    )
                }

                composable<Screen.User> {
                    CivitAiUserScreen(username = it.toRoute<Screen.User>().username)
                }

                composable<Screen.Favorites> { FavoritesUI() }

                navigation<Screen.Settings>(
                    startDestination = Screen.Settings.Screen
                ) {
                    composable<Screen.Settings.Screen>(
                        enterTransition = { slideIntoContainer(AnimatedContentTransitionScope.SlideDirection.Down) },
                        exitTransition = { slideOutOfContainer(AnimatedContentTransitionScope.SlideDirection.Up) }
                    ) {
                        SettingsScreen(
                            onExport = onExport,
                            onImport = onImport,
                            export = export,
                            import = import
                        )
                    }

                    composable<Screen.Blacklisted>(
                        enterTransition = { slideIntoContainer(AnimatedContentTransitionScope.SlideDirection.Up) },
                        exitTransition = { slideOutOfContainer(AnimatedContentTransitionScope.SlideDirection.Down) }
                    ) { BlacklistedScreen() }
                }
            }
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
    data object List

    @Serializable
    class Detail(val modelId: String)

    @Serializable
    data object Settings {
        @Serializable
        data object Screen
    }

    @Serializable
    data object Favorites

    @Serializable
    class User(val username: String)

    @Serializable
    class DetailsImage(val modelId: String, val modelName: String)

    @Serializable
    data object Blacklisted
}