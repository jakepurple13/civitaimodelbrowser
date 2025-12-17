package com.programmersbox.common.di

import androidx.compose.material3.adaptive.ExperimentalMaterial3AdaptiveApi
import androidx.compose.material3.adaptive.navigation3.ListDetailSceneStrategy
import androidx.navigation3.scene.DialogSceneStrategy
import com.programmersbox.common.NavigationHandler
import com.programmersbox.common.Screen
import com.programmersbox.common.SettingsScreen
import com.programmersbox.common.StatsScreen
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
import org.koin.compose.koinInject
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.annotation.KoinExperimentalAPI
import org.koin.core.module.dsl.singleOf
import org.koin.core.parameter.parametersOf
import org.koin.dsl.module
import org.koin.dsl.navigation3.navigation

@OptIn(ExperimentalMaterial3AdaptiveApi::class, KoinExperimentalAPI::class)
fun navigationModule() = module {
    singleOf(::NavigationHandler)

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
            onNavigateToBlacklisted = {
                backStack.add(Screen.Settings)
                backStack.add(Screen.Settings.Blacklisted)
            },
            onNavigateToCustomList = { backStack.add(Screen.CustomList) },
        )
    }

    navigation<Screen.Detail>(
        metadata = ListDetailSceneStrategy.listPane()
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
    navigation<Screen.Settings.Blacklisted> { BlacklistedScreen() }
    navigation<Screen.Settings> {
        val backStack = koinInject<NavigationHandler>().backStack
        SettingsScreen(
            onNavigateToQrCode = { backStack.add(Screen.QrCode) },
            onNavigateToBackup = { backStack.add(Screen.Backup) },
            onNavigateToRestore = { backStack.add(Screen.Restore) },
            onNavigateToStats = { backStack.add(Screen.Stats) }
        )
    }
    navigation<Screen.Settings.Screen> {
        val backStack = koinInject<NavigationHandler>().backStack
        SettingsScreen(
            onNavigateToQrCode = { backStack.add(Screen.QrCode) },
            onNavigateToBackup = { backStack.add(Screen.Backup) },
            onNavigateToRestore = { backStack.add(Screen.Restore) },
            onNavigateToStats = { backStack.add(Screen.Stats) }
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
    navigation<Screen.Images> {
        val backStack = koinInject<NavigationHandler>().backStack
        CivitAiImagesScreen(
            onNavigateToUser = { username -> backStack.add(Screen.User(username)) }
        )
    }
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

    navigation<Screen.Backup> { BackupScreen() }
    navigation<Screen.Restore> { RestoreScreen() }
    navigation<Screen.Stats> { StatsScreen() }
}