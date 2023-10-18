@file:Suppress("INLINE_FROM_HIGHER_PLATFORM")

package com.programmersbox.common

import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.remember
import androidx.compose.runtime.staticCompositionLocalOf
import com.programmersbox.common.creator.CivitAiUserScreen
import com.programmersbox.common.db.FavoritesDatabase
import com.programmersbox.common.db.FavoritesUI
import com.programmersbox.common.details.CivitAiDetailScreen
import com.programmersbox.common.details.CivitAiModelImagesScreen
import com.programmersbox.common.home.CivitAiScreen
import moe.tlaster.precompose.navigation.*
import moe.tlaster.precompose.navigation.transition.NavTransition
import moe.tlaster.precompose.viewmodel.ViewModel
import moe.tlaster.precompose.viewmodel.viewModel

@Composable
internal fun App(
    onShareClick: (String) -> Unit,
    producePath: () -> String,
) {
    val navController = rememberNavigator()
    val viewModel = viewModel { AppViewModel(DataStore.getStore(producePath)) }
    CompositionLocalProvider(
        LocalNavController provides navController,
        LocalDataStore provides viewModel.dataStore,
        LocalDatabase provides remember { FavoritesDatabase() }
    ) {
        Surface {
            NavHost(
                navigator = navController,
                initialRoute = Screen.List.routeId,
                navTransition = NavTransition(
                    createTransition = slideInHorizontally { it },
                    destroyTransition = slideOutHorizontally { it },
                    resumeTransition = slideInHorizontally { -it },
                    pauseTransition = slideOutHorizontally { -it },
                )
            ) {
                scene(Screen.List.routeId) { CivitAiScreen() }
                group(
                    "detailsgroup",
                    Screen.Detail.routeId
                ) {
                    scene(Screen.Detail.routeId) {
                        CivitAiDetailScreen(
                            id = it.path<String>("modelId"),
                            onShareClick = onShareClick
                        )
                    }

                    scene(Screen.DetailsImage.routeId) {
                        CivitAiModelImagesScreen(
                            modelId = it.path<String>("modelId"),
                            modelName = it.query<String>("modelName")
                        )
                    }
                }
                scene(Screen.Settings.routeId) { SettingsScreen() }
                scene(Screen.Favorites.routeId) { FavoritesUI() }
                scene(Screen.User.routeId) {
                    CivitAiUserScreen(username = it.path<String>("username").orEmpty())
                }
            }
        }
    }
}

class AppViewModel(val dataStore: DataStore) : ViewModel()

internal val LocalNavController = staticCompositionLocalOf<Navigator> { error("Nope") }
internal val LocalDataStore = staticCompositionLocalOf<DataStore> { error("Nope") }
internal val LocalDatabase = staticCompositionLocalOf<FavoritesDatabase> { error("Nope") }
internal val LocalNetwork = staticCompositionLocalOf { Network() }

fun Navigator.navigateToDetail(id: Long) {
    navigate(
        route = Screen.Detail.routeId.replace("{modelId}", id.toString()),
        options = NavOptions(launchSingleTop = true, includePath = true)
    )
}

fun Navigator.navigateToDetailImages(id: Long, name: String) {
    navigate(
        route = Screen.DetailsImage.routeId.replace("{modelId}", id.toString()) + "?modelName=$name",
        options = NavOptions(launchSingleTop = true, includePath = true)
    )
}

fun Navigator.navigateToUser(username: String) {
    navigate(
        route = Screen.User.routeId.replace("{username}", username),
        options = NavOptions(launchSingleTop = true)
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