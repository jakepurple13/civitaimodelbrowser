package com.programmersbox.common

import androidx.compose.animation.SharedTransitionLayout
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.material3.adaptive.ExperimentalMaterial3AdaptiveApi
import androidx.compose.material3.adaptive.navigation.BackNavigationBehavior
import androidx.compose.material3.adaptive.navigation3.rememberListDetailSceneStrategy
import androidx.compose.material3.adaptive.navigation3.rememberSupportingPaneSceneStrategy
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.navigation3.rememberViewModelStoreNavEntryDecorator
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.rememberSaveableStateHolderNavEntryDecorator
import androidx.navigation3.scene.DialogSceneStrategy
import androidx.navigation3.ui.NavDisplay
import com.programmersbox.common.di.NavigationHandler
import dev.chrisbanes.haze.LocalHazeStyle
import dev.chrisbanes.haze.materials.HazeMaterials
import kotlinx.coroutines.flow.collect
import kotlinx.serialization.Serializable
import org.koin.compose.koinInject
import org.koin.compose.navigation3.koinEntryProvider

@OptIn(ExperimentalMaterial3AdaptiveApi::class)
@Composable
internal fun App(
    onShareClick: (String) -> Unit,
) {
    SetupNetworkListener()
    CompositionLocalProvider(
        LocalHazeStyle provides HazeMaterials.regular(),
        LocalActions provides remember {
            Actions(
                shareUrl = onShareClick,
            )
        }
    ) {
        SharedTransitionLayout {

            val listDetailSceneStrategy = rememberListDetailSceneStrategy<Any>(
                backNavigationBehavior = BackNavigationBehavior.PopLatest
            )

            val supportingPaneSceneStrategy = rememberSupportingPaneSceneStrategy<Any>(
                backNavigationBehavior = BackNavigationBehavior.PopLatest
            )

            Surface {
                NavDisplay(
                    backStack = koinInject<NavigationHandler>().backStack,
                    entryDecorators = listOf(
                        rememberSaveableStateHolderNavEntryDecorator(),
                        rememberViewModelStoreNavEntryDecorator()
                    ),
                    sceneStrategy = listDetailSceneStrategy
                            then supportingPaneSceneStrategy
                            then DialogSceneStrategy(),
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

@Composable
private fun SetupNetworkListener() {
    val networkConnectionRepository = koinInject<NetworkConnectionRepository>()
    LaunchedEffect(Unit) {
        networkConnectionRepository
            .connectivityFlow()
            .collect()
    }
    DisposableEffect(Unit) {
        networkConnectionRepository.start()
        onDispose { networkConnectionRepository.stop() }
    }
}

val LocalActions = staticCompositionLocalOf<Actions> { error("Nothing") }

data class Actions(
    val shareUrl: (String) -> Unit,
)

sealed class Screen {
    @Serializable
    data object List : NavKey

    @Serializable
    data class Detail(val modelId: String) : NavKey

    @Serializable
    data object Settings : NavKey {

        @Serializable
        data object Blacklisted : NavKey

        @Serializable
        data object Nsfw : NavKey

        @Serializable
        data object Behavior : NavKey

        @Serializable
        data object Backup : NavKey

        @Serializable
        data object Restore : NavKey

        @Serializable
        data object Stats : NavKey

        @Serializable
        data object About : NavKey

        @Serializable
        data object BluetoothTransfer : NavKey
    }

    @Serializable
    data object Favorites : NavKey

    @Serializable
    data class User(val username: String) : NavKey

    @Serializable
    data class DetailsImage(val modelId: String, val modelName: String) : NavKey

    @Serializable
    data object QrCode : NavKey

    @Serializable
    data object Images : NavKey

    @Serializable
    data object CustomList : NavKey

    @Serializable
    data class CustomListDetail(val uuid: String) : NavKey

    @Serializable
    data object Search : NavKey
}