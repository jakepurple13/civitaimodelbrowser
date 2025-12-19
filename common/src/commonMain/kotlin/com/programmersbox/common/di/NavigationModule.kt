package com.programmersbox.common.di

import androidx.compose.material3.adaptive.ExperimentalMaterial3AdaptiveApi
import androidx.compose.material3.adaptive.navigation3.ListDetailSceneStrategy
import androidx.compose.material3.adaptive.navigation3.SupportingPaneSceneStrategy
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateListOf
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.scene.DialogSceneStrategy
import com.programmersbox.common.Screen
import com.programmersbox.common.backup.BackupScreen
import com.programmersbox.common.backup.RestoreScreen
import com.programmersbox.common.blacklisted.BlacklistedScreen
import com.programmersbox.common.creator.CivitAiUserScreen
import com.programmersbox.common.db.FavoritesUI
import com.programmersbox.common.details.CivitAiDetailScreen
import com.programmersbox.common.details.CivitAiModelImagesScreen
import com.programmersbox.common.home.CivitAiScreen
import com.programmersbox.common.images.CivitAiImagesScreen
import com.programmersbox.common.lists.ListDetailScreen
import com.programmersbox.common.lists.ListScreen
import com.programmersbox.common.qrcode.ScanQrCode
import com.programmersbox.common.settings.AboutScreen
import com.programmersbox.common.settings.BehaviorSettingsScreen
import com.programmersbox.common.settings.NsfwSettingsScreen
import com.programmersbox.common.settings.SettingsScreen
import com.programmersbox.common.settings.StatsScreen
import org.koin.compose.koinInject
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.annotation.KoinExperimentalAPI
import org.koin.core.module.Module
import org.koin.core.module.dsl.singleOf
import org.koin.core.parameter.parametersOf
import org.koin.core.scope.Scope
import org.koin.dsl.module
import org.koin.dsl.navigation3.navigation

@OptIn(ExperimentalMaterial3AdaptiveApi::class, KoinExperimentalAPI::class)
fun navigationModule() = module {
    singleOf(::NavigationHandler)
    home()
    details()
    customList()
    settingsNavigation()
}

@OptIn(KoinExperimentalAPI::class)
private fun Module.home() {
    navigation<Screen.List> {
        val backStack = koinInject<NavigationHandler>().backStack
        CivitAiScreen(
            onNavigateToDetail = { id -> backStack.add(Screen.Detail(id)) },
            onNavigateToFavorites = { backStack.add(Screen.Favorites) },
            onNavigateToSettings = { backStack.add(Screen.Settings) },
            onNavigateToQrCode = { backStack.add(Screen.QrCode) },
            onNavigateToUser = { username -> backStack.add(Screen.User(username)) },
            onNavigateToImages = { backStack.add(Screen.Images) },
            onNavigateToDetailImages = { id, name ->
                backStack.addAll(
                    listOf(
                        Screen.Detail(id.toString()),
                        Screen.DetailsImage(id.toString(), name)
                    )
                )
            },
            onNavigateToBlacklist = {
                backStack.add(Screen.Settings)
                backStack.add(Screen.Settings.Blacklisted)
            },
            onNavigateToCustomList = { backStack.add(Screen.CustomList) },
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
    navigation<Screen.QrCode>(
        metadata = DialogSceneStrategy.dialog()
    ) {
        val backStack = koinInject<NavigationHandler>().backStack
        ScanQrCode(
            viewModel = koinViewModel(),
            onBack = { backStack.removeLastOrNull() },
            onNavigate = { navKey ->
                backStack.removeAll { it == Screen.QrCode }
                backStack.add(navKey)
            }
        )
    }
    navigation<Screen.Images> {
        val backStack = koinInject<NavigationHandler>().backStack
        CivitAiImagesScreen(
            onNavigateToUser = { username -> backStack.add(Screen.User(username)) }
        )
    }
}

@OptIn(KoinExperimentalAPI::class, ExperimentalMaterial3AdaptiveApi::class)
private fun Module.customList() {
    navigation<Screen.CustomList>(
        metadata = ListDetailSceneStrategy.listPane()
    ) {
        val backStack = koinInject<NavigationHandler>().backStack
        ListScreen(
            onNavigateToDetail = { id -> backStack.add(Screen.CustomListDetail(id)) }
        )
    }
    navigation<Screen.CustomListDetail>(
        metadata = ListDetailSceneStrategy.detailPane()
    ) {
        val backStack = koinInject<NavigationHandler>().backStack
        ListDetailScreen(
            viewModel = koinViewModel { parametersOf(it.uuid) },
            onNavigateToDetail = { id -> backStack.add(Screen.Detail(id)) },
            onNavigateToUser = { username -> backStack.add(Screen.User(username)) }
        )
    }
}

@OptIn(ExperimentalMaterial3AdaptiveApi::class, KoinExperimentalAPI::class)
private fun Module.details() {
    navigation<Screen.Detail>(
        metadata = SupportingPaneSceneStrategy.mainPane(sceneKey = "detail")
    ) {
        val backStack = koinInject<NavigationHandler>().backStack
        CivitAiDetailScreen(
            id = it.modelId,
            viewModel = koinViewModel { parametersOf(it.modelId) },
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
        metadata = SupportingPaneSceneStrategy.extraPane(sceneKey = "detail")
    ) {
        CivitAiModelImagesScreen(
            modelName = it.modelName,
            viewModel = koinViewModel { parametersOf(it.modelId) }
        )
    }

    navigation<Screen.User>(
        metadata = SupportingPaneSceneStrategy.extraPane(sceneKey = "detail")
    ) {
        val backStack = koinInject<NavigationHandler>().backStack
        CivitAiUserScreen(
            viewModel = koinViewModel { parametersOf(it.username) },
            username = it.username,
            onNavigateToDetail = { id -> backStack.add(Screen.Detail(id)) }
        )
    }
}

@OptIn(ExperimentalMaterial3AdaptiveApi::class, KoinExperimentalAPI::class)
private fun Module.settingsNavigation() {
    navigation<Screen.Settings>(
        metadata = ListDetailSceneStrategy.listPane(
            sceneKey = "settings"
        )
    ) {
        val backStack = koinInject<NavigationHandler>().backStack
        SettingsScreen(
            onNavigateToQrCode = { backStack.add(Screen.QrCode) },
            onNavigateToBackup = { backStack.add(Screen.Settings.Backup) },
            onNavigateToRestore = { backStack.add(Screen.Settings.Restore) },
            onNavigateToStats = { backStack.add(Screen.Settings.Stats) },
            onNavigateToAbout = { backStack.add(Screen.Settings.About) },
            onNavigateToNsfw = { backStack.add(Screen.Settings.Nsfw) },
            onNavigateToBehavior = { backStack.add(Screen.Settings.Behavior) }
        )
    }

    settingsNavigation<Screen.Settings.Blacklisted> { BlacklistedScreen() }
    settingsNavigation<Screen.Settings.Backup> { BackupScreen() }
    settingsNavigation<Screen.Settings.Restore> { RestoreScreen() }
    settingsNavigation<Screen.Settings.Stats> { StatsScreen() }
    settingsNavigation<Screen.Settings.About> { AboutScreen() }
    settingsNavigation<Screen.Settings.Nsfw> { NsfwSettingsScreen() }
    settingsNavigation<Screen.Settings.Behavior> { BehaviorSettingsScreen() }
}

@OptIn(ExperimentalMaterial3AdaptiveApi::class, KoinExperimentalAPI::class)
private inline fun <reified T : NavKey> Module.settingsNavigation(
    metadata: Map<String, Any> = emptyMap(),
    noinline definition: @Composable Scope.(T) -> Unit,
) {
    navigation<T>(
        metadata = ListDetailSceneStrategy.detailPane(sceneKey = "settings") + metadata,
        definition = definition
    )
}

class NavigationHandler {
    val backStack = mutableStateListOf<NavKey>(Screen.List)
}