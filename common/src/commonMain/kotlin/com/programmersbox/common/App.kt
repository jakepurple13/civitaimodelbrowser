@file:Suppress("INLINE_FROM_HIGHER_PLATFORM")

package com.programmersbox.common

import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.remember
import androidx.compose.runtime.staticCompositionLocalOf
import moe.tlaster.precompose.navigation.NavHost
import moe.tlaster.precompose.navigation.Navigator
import moe.tlaster.precompose.navigation.path
import moe.tlaster.precompose.navigation.rememberNavigator
import moe.tlaster.precompose.navigation.transition.NavTransition
import moe.tlaster.precompose.viewmodel.ViewModel
import moe.tlaster.precompose.viewmodel.viewModel

@Composable
internal fun App(
    onShareClick: (String) -> Unit,
    producePath: () -> String,
) {
    val navController = rememberNavigator()
    val network = remember { Network() }
    val viewModel = viewModel { AppViewModel(DataStore(producePath)) }
    CompositionLocalProvider(
        LocalNavController provides navController,
        LocalDataStore provides viewModel.dataStore
    ) {
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
            scene(Screen.List.routeId) { CivitAiScreen(network) }
            scene(Screen.Detail.routeId) {
                CivitAiDetailScreen(
                    network = network,
                    id = it.path<String>("modelId"),
                    onShareClick = onShareClick
                )
            }
            scene(Screen.Settings.routeId) { SettingsScreen() }
        }
    }
}

class AppViewModel(val dataStore: DataStore) : ViewModel()

internal val LocalNavController = staticCompositionLocalOf<Navigator> { error("Nope") }
internal val LocalDataStore = staticCompositionLocalOf<DataStore> { error("Nope") }

enum class Screen(val routeId: String) {
    List("list"),
    Detail("detail/{modelId}"),
    Settings("settings")
}