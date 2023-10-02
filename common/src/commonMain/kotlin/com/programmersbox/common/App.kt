@file:Suppress("INLINE_FROM_HIGHER_PLATFORM")

package com.programmersbox.common

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.remember
import androidx.compose.runtime.staticCompositionLocalOf
import moe.tlaster.precompose.navigation.NavHost
import moe.tlaster.precompose.navigation.Navigator
import moe.tlaster.precompose.navigation.path
import moe.tlaster.precompose.navigation.rememberNavigator

@Composable
internal fun App(
    onShareClick: (String) -> Unit,
) {
    val navController = rememberNavigator()
    val network = remember { Network() }
    CompositionLocalProvider(
        LocalNavController provides navController
    ) {
        NavHost(
            navigator = navController,
            initialRoute = Screen.List.routeId
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

internal val LocalNavController = staticCompositionLocalOf<Navigator> { error("Nope") }

enum class Screen(val routeId: String) {
    List("list"),
    Detail("detail/{modelId}"),
    Settings("settings")
}