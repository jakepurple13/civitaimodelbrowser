package com.programmersbox.common

import androidx.compose.animation.SharedTransitionLayout
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.material3.adaptive.ExperimentalMaterial3AdaptiveApi
import androidx.compose.material3.adaptive.navigation3.rememberListDetailSceneStrategy
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.navigation3.rememberViewModelStoreNavEntryDecorator
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.rememberSaveableStateHolderNavEntryDecorator
import androidx.navigation3.scene.DialogSceneStrategy
import androidx.navigation3.ui.NavDisplay
import com.dokar.sonner.ToasterState
import com.programmersbox.common.db.AppDatabase
import com.programmersbox.common.db.getRoomDatabase
import com.programmersbox.common.di.navigationModule
import com.programmersbox.common.di.repositoryModule
import com.programmersbox.common.di.viewModelModule
import dev.chrisbanes.haze.LocalHazeStyle
import dev.chrisbanes.haze.materials.HazeMaterials
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.collect
import kotlinx.serialization.Serializable
import org.koin.compose.koinInject
import org.koin.compose.navigation3.koinEntryProvider
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.module

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
            Surface {
                NavDisplay(
                    backStack = koinInject<NavigationHandler>().backStack,
                    entryDecorators = listOf(
                        rememberSaveableStateHolderNavEntryDecorator(),
                        rememberViewModelStoreNavEntryDecorator()
                    ),
                    sceneStrategy = rememberListDetailSceneStrategy<Any>()
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

@OptIn(ExperimentalMaterial3AdaptiveApi::class)
fun cmpModules() = module {
    singleOf(::Network)
    single { DataStore.getStore(get()) }
    single { getRoomDatabase(get()) }
    single { get<AppDatabase>().getDao() }
    single { get<AppDatabase>().getListDao() }
    single { ToasterState(CoroutineScope(Dispatchers.Main)) }

    includes(
        viewModelModule(),
        repositoryModule(),
        navigationModule()
    )
}

class NavigationHandler {
    val backStack = mutableStateListOf<NavKey>(Screen.List)
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
        data object Screen : NavKey

        @Serializable
        data object Blacklisted : NavKey
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
    data object Backup : NavKey

    @Serializable
    data object Restore : NavKey

    @Serializable
    data object Stats : NavKey

    @Serializable
    data object About : NavKey
}