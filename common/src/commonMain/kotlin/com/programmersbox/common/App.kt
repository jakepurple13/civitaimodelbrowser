package com.programmersbox.common

import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.material3.adaptive.ExperimentalMaterial3AdaptiveApi
import androidx.compose.material3.adaptive.navigation3.ListDetailSceneStrategy
import androidx.compose.material3.adaptive.navigation3.rememberListDetailSceneStrategy
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.navigation3.rememberViewModelStoreNavEntryDecorator
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.rememberSaveableStateHolderNavEntryDecorator
import androidx.navigation3.scene.DialogSceneStrategy
import androidx.navigation3.ui.NavDisplay
import com.programmersbox.common.blacklisted.BlacklistedScreen
import com.programmersbox.common.creator.CivitAiUserScreen
import com.programmersbox.common.creator.CivitAiUserViewModel
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
import com.programmersbox.common.qrcode.QrCodeScannerViewModel
import com.programmersbox.common.qrcode.ScanQrCode
import dev.chrisbanes.haze.LocalHazeStyle
import dev.chrisbanes.haze.materials.HazeMaterials
import kotlinx.serialization.Serializable
import org.koin.compose.koinInject
import org.koin.compose.navigation3.koinEntryProvider
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.module.dsl.singleOf
import org.koin.core.module.dsl.viewModel
import org.koin.core.module.dsl.viewModelOf
import org.koin.core.parameter.parametersOf
import org.koin.dsl.module
import org.koin.dsl.navigation3.navigation

@OptIn(ExperimentalMaterial3AdaptiveApi::class)
@Composable
internal fun App(
    onShareClick: (String) -> Unit,
    onExport: (CivitDb) -> Unit = {},
    onImport: () -> String = { "" },
    export: @Composable () -> Unit = {},
    import: (@Composable () -> Unit)? = null,
) {
    //val backStack = remember { mutableStateListOf<NavKey>(Screen.List) }
    CompositionLocalProvider(
        LocalHazeStyle provides HazeMaterials.regular(),
        LocalDatabaseDao provides koinInject(),
        LocalActions provides remember {
            Actions(
                shareUrl = onShareClick,
                onExport = onExport,
                onImport = onImport,
                export = export,
                import = import,
            )
        }
    ) {
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
                /*entryProvider = entryProvider {
                    entry<Screen.List> {
                        CivitAiScreen(
                            onNavigateToDetail = { id -> backStack.add(Screen.Detail(id)) },
                            onNavigateToFavorites = { backStack.add(Screen.Favorites) },
                            onNavigateToSettings = { backStack.add(Screen.Settings) }
                        )
                    }

                    entry<Screen.Detail>(
                        metadata = ListDetailSceneStrategy.listPane()
                    ) {
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

                    entry<Screen.DetailsImage>(
                        metadata = ListDetailSceneStrategy.detailPane()
                    ) {
                        CivitAiModelImagesScreen(
                            modelName = it.modelName,
                            viewModel = koinViewModel { parametersOf(it.modelId) }
                        )
                    }

                    entry<Screen.User>(
                        metadata = ListDetailSceneStrategy.extraPane()
                    ) {
                        CivitAiUserScreen(
                            viewModel = koinViewModel { parametersOf(it.username) },
                            username = it.username,
                            onNavigateToDetail = { id -> backStack.add(Screen.Detail(id)) }
                        )
                    }
                    entry<Screen.Favorites> {
                        FavoritesUI(
                            viewModel = koinViewModel(),
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
                            onNavigateToBlacklisted = { backStack.add(Screen.Settings.Blacklisted) },
                            onNavigateToQrCode = { backStack.add(Screen.QrCode) }
                        )
                    }
                    entry<Screen.Settings.Blacklisted> { BlacklistedScreen() }
                    entry<Screen.Settings.Screen> {
                        SettingsScreen(
                            onExport = onExport,
                            onImport = onImport,
                            export = export,
                            import = import,
                            onNavigateToBlacklisted = { backStack.add(Screen.Settings.Blacklisted) },
                            onNavigateToQrCode = { backStack.add(Screen.QrCode) }
                        )
                    }
                    entry<Screen.QrCode>(
                        metadata = DialogSceneStrategy.dialog()
                    ) {
                        ScanQrCode(
                            viewModel = koinViewModel(),
                            onBack = { backStack.removeLastOrNull() },
                            onNavigate = { navKey ->
                                backStack.removeIf { it == Screen.QrCode }
                                backStack.add(navKey)
                            }
                        )
                    }
                },*/
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

@OptIn(ExperimentalMaterial3AdaptiveApi::class)
fun cmpModules() = module {
    singleOf(::Network)
    single { DataStore.getStore(get()) }
    single { getRoomDatabase(get()).getDao() }
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
    viewModelOf(::QrCodeScannerViewModel)

    singleOf(::NavigationHandler)

    navigation<Screen.List> {
        val backStack = koinInject<NavigationHandler>().backStack
        CivitAiScreen(
            onNavigateToDetail = { id -> backStack.add(Screen.Detail(id)) },
            onNavigateToFavorites = { backStack.add(Screen.Favorites) },
            onNavigateToSettings = { backStack.add(Screen.Settings) }
        )
    }

    navigation<Screen.Detail>(
        metadata = ListDetailSceneStrategy.listPane()
    ) {
        val backStack = koinInject<NavigationHandler>().backStack
        val actions = LocalActions.current
        CivitAiDetailScreen(
            id = it.modelId,
            viewModel = koinViewModel { parametersOf(it.modelId) },
            onShareClick = actions.shareUrl,
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

    navigation<Screen.DetailsImage>(
        metadata = ListDetailSceneStrategy.detailPane()
    ) {
        CivitAiModelImagesScreen(
            modelName = it.modelName,
            viewModel = koinViewModel { parametersOf(it.modelId) }
        )
    }

    navigation<Screen.User>(
        metadata = ListDetailSceneStrategy.extraPane()
    ) {
        val backStack = koinInject<NavigationHandler>().backStack
        CivitAiUserScreen(
            viewModel = koinViewModel { parametersOf(it.username) },
            username = it.username,
            onNavigateToDetail = { id -> backStack.add(Screen.Detail(id)) }
        )
    }
    navigation<Screen.Favorites> {
        val backStack = koinInject<NavigationHandler>().backStack
        FavoritesUI(
            viewModel = koinViewModel(),
            onNavigateToDetail = { id -> backStack.add(Screen.Detail(id.toString())) },
            onNavigateToUser = { username -> backStack.add(Screen.User(username)) }
        )
    }
    navigation<Screen.Settings> {
        val backStack = koinInject<NavigationHandler>().backStack
        val actions = LocalActions.current
        SettingsScreen(
            onExport = actions.onExport,
            onImport = actions.onImport,
            export = actions.export,
            import = actions.import,
            onNavigateToBlacklisted = { backStack.add(Screen.Settings.Blacklisted) },
            onNavigateToQrCode = { backStack.add(Screen.QrCode) }
        )
    }
    navigation<Screen.Settings.Blacklisted> { BlacklistedScreen() }
    navigation<Screen.Settings.Screen> {
        val backStack = koinInject<NavigationHandler>().backStack
        val actions = LocalActions.current
        SettingsScreen(
            onExport = actions.onExport,
            onImport = actions.onImport,
            export = actions.export,
            import = actions.import,
            onNavigateToBlacklisted = { backStack.add(Screen.Settings.Blacklisted) },
            onNavigateToQrCode = { backStack.add(Screen.QrCode) }
        )
    }
    navigation<Screen.QrCode>(
        metadata = DialogSceneStrategy.dialog()
    ) {
        val backStack = koinInject<NavigationHandler>().backStack
        ScanQrCode(
            viewModel = koinViewModel(),
            onBack = { backStack.removeLastOrNull() },
            onNavigate = { navKey ->
                backStack.removeIf { it == Screen.QrCode }
                backStack.add(navKey)
            }
        )
    }
}

class NavigationHandler {
    val backStack = mutableStateListOf<NavKey>(Screen.List)
}

val LocalActions = staticCompositionLocalOf<Actions> { error("Nothing") }

data class Actions(
    val shareUrl: (String) -> Unit,
    val onImport: () -> String,
    val onExport: (CivitDb) -> Unit,
    val export: @Composable () -> Unit,
    val import: (@Composable () -> Unit)? = null,
)

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

    @Serializable
    data object QrCode : NavKey
}