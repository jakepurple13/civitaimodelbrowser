@file:Suppress("INLINE_FROM_HIGHER_PLATFORM")

package com.programmersbox.common

import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.NavOptions
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.navigation
import androidx.navigation.compose.rememberNavController
import com.programmersbox.common.creator.CivitAiUserScreen
import com.programmersbox.common.db.FavoriteModel
import com.programmersbox.common.db.FavoritesDatabase
import com.programmersbox.common.db.FavoritesUI
import com.programmersbox.common.details.CivitAiDetailScreen
import com.programmersbox.common.details.CivitAiModelImagesScreen
import com.programmersbox.common.home.CivitAiScreen
import moe.tlaster.precompose.PreComposeApp

@Composable
internal fun App(
    onShareClick: (String) -> Unit,
    producePath: () -> String,
    onExport: (List<FavoriteModel>) -> Unit = {},
    onImport: () -> String = { "" },
    export: @Composable () -> Unit = {},
    import: @Composable () -> Unit = {},
) {
    PreComposeApp {
        val navController = rememberNavController()
        val viewModel = viewModel { AppViewModel(DataStore.getStore(producePath)) }
        CompositionLocalProvider(
            LocalNavController provides navController,
            LocalDataStore provides viewModel.dataStore
        ) {
            Surface {
                NavHost(
                    navController = navController,
                    startDestination = Screen.List.routeId,
                    enterTransition = { slideInHorizontally { it } },
                    exitTransition = { slideOutHorizontally { it } },
                    popEnterTransition = { slideInHorizontally { -it } },
                    popExitTransition = { slideOutHorizontally { -it } },
                    /*navTransition = NavTransition(
                        createTransition = slideInHorizontally { it },
                        destroyTransition = slideOutHorizontally { it },
                        resumeTransition = slideInHorizontally { -it },
                        pauseTransition = slideOutHorizontally { -it },
                    )*/
                ) {
                    composable(Screen.List.routeId) { CivitAiScreen() }
                    navigation(
                        startDestination = Screen.Detail.routeId,
                        route = "detailsgroup",
                    ) {
                        composable(Screen.Detail.routeId) {
                            CivitAiDetailScreen(
                                id = "",//it.path<String>("modelId"),
                                onShareClick = onShareClick
                            )
                        }

                        composable(Screen.DetailsImage.routeId) {
                            CivitAiModelImagesScreen(
                                modelId = "",//it.path<String>("modelId"),
                                modelName = ""//it.query<String>("modelName")
                            )
                        }
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
                        //CivitAiUserScreen(username = it.path<String>("username").orEmpty())
                        CivitAiUserScreen()
                    }
                }
            }
        }
    }
}

class AppViewModel(val dataStore: DataStore) : ViewModel()

//internal val LocalNavController = staticCompositionLocalOf<Navigator> { error("Nope") }
internal val LocalNavController = staticCompositionLocalOf<NavHostController> { error("Nope") }
internal val LocalDataStore = staticCompositionLocalOf<DataStore> { error("Nope") }
val LocalDatabase = staticCompositionLocalOf<FavoritesDatabase> { FavoritesDatabase() }
internal val LocalNetwork = staticCompositionLocalOf { Network() }

fun NavHostController.navigateToDetail(id: Long) {
    navigate(
        route = Screen.Detail.routeId.replace("{modelId}", id.toString()),
        navOptions = NavOptions.Builder()
            .setLaunchSingleTop(true)
            .build()
    )
}

fun NavHostController.navigateToDetailImages(id: Long, name: String) {
    navigate(
        route = Screen.DetailsImage.routeId.replace("{modelId}", id.toString()) + "?modelName=$name",
        navOptions = NavOptions.Builder()
            .setLaunchSingleTop(true)
            .build()
    )
}

fun NavHostController.navigateToUser(username: String) {
    navigate(
        route = Screen.User.routeId.replace("{username}", username),
        navOptions = androidx.navigation.NavOptions.Builder()
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
    DetailsImage("detailsimage/{modelId}")
}