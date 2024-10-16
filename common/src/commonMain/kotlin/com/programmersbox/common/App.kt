package com.programmersbox.common

import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
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
import androidx.navigation.compose.rememberNavController
import androidx.room.RoomDatabase
import com.programmersbox.common.blacklisted.BlacklistedScreen
import com.programmersbox.common.creator.CivitAiUserScreen
import com.programmersbox.common.db.*
import com.programmersbox.common.details.CivitAiDetailScreen
import com.programmersbox.common.details.CivitAiModelImagesScreen
import com.programmersbox.common.home.CivitAiScreen
import dev.chrisbanes.haze.LocalHazeStyle
import dev.chrisbanes.haze.materials.HazeMaterials

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
                startDestination = Screen.List.routeId,
                enterTransition = { slideInHorizontally { it } },
                exitTransition = { slideOutHorizontally { it } },
                popEnterTransition = { slideInHorizontally { it } },
                popExitTransition = { slideOutHorizontally { it } },
            ) {
                composable(Screen.List.routeId) { CivitAiScreen() }
                composable(Screen.Detail.routeId) {
                    CivitAiDetailScreen(
                        id = it.arguments?.getString("modelId"),
                        onShareClick = onShareClick
                    )
                }

                composable(Screen.DetailsImage.routeId) {
                    CivitAiModelImagesScreen(
                        modelId = it.arguments?.getString("modelId"),
                        modelName = it.arguments?.getString("modelName")
                    )
                }
                composable(Screen.Settings.routeId) {
                    SettingsScreen(
                        onExport = onExport,
                        onImport = onImport,
                        export = export,
                        import = import
                    )
                }
                composable(Screen.Favorites.routeId) { FavoritesUI() }
                composable(Screen.User.routeId) {
                    CivitAiUserScreen(username = it.arguments?.getString("username").orEmpty())
                }
                composable(Screen.Blacklisted.routeId) { BlacklistedScreen() }
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
        route = Screen.Detail.routeId.replace("{modelId}", id.toString()),
        navOptions = NavOptions.Builder()
            .setLaunchSingleTop(true)
            .build()
    )
}

fun NavController.navigateToDetailImages(id: Long, name: String) {
    navigate(
        route = Screen.DetailsImage.routeId.replace("{modelId}", id.toString()) + "?modelName=$name",
        navOptions = NavOptions.Builder()
            .setLaunchSingleTop(true)
            .build()
    )
}

fun NavController.navigateToUser(username: String) {
    navigate(
        route = Screen.User.routeId.replace("{username}", username),
        navOptions = NavOptions.Builder()
            .setLaunchSingleTop(true)
            .build()
    )
}

enum class Screen(val routeId: String) {
    List("list"),
    Detail("detail/{modelId}"),
    Settings("settings"),
    Favorites("favorites"),
    User("user/{username}"),
    DetailsImage("detailsimage/{modelId}"),
    Blacklisted("blacklisted"),
}