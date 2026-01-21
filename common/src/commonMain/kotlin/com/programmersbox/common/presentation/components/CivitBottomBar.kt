package com.programmersbox.common.presentation.components

import androidx.compose.foundation.layout.RowScope
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.automirrored.outlined.List
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.BottomAppBar
import androidx.compose.material3.BottomAppBarDefaults
import androidx.compose.material3.BottomAppBarScrollBehavior
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationRail
import androidx.compose.material3.NavigationRailItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.navigation3.runtime.NavKey
import com.programmersbox.common.Screen
import com.programmersbox.common.di.NavigationHandler
import com.programmersbox.resources.Res
import com.programmersbox.resources.favorites
import com.programmersbox.resources.home
import com.programmersbox.resources.lists
import com.programmersbox.resources.search
import com.programmersbox.resources.settings
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.koinInject

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CivitBottomBar(
    showBlur: Boolean,
    bottomBarScrollBehavior: BottomAppBarScrollBehavior?,
    modifier: Modifier = Modifier,
) {
    val backStack = koinInject<NavigationHandler>().backStack
    val currentScreen = backStack.lastOrNull()

    @Composable
    fun RowScope.NavBarItem(
        screen: NavKey,
        onClick: () -> Unit,
        icon: @Composable (Boolean) -> Unit,
        label: String,
    ) {
        NavigationBarItem(
            selected = screen == currentScreen,
            onClick = onClick,
            icon = { icon(screen == currentScreen) },
            label = { Text(label) },
        )
    }

    BottomAppBar(
        containerColor = if (showBlur) Color.Transparent else BottomAppBarDefaults.containerColor,
        scrollBehavior = bottomBarScrollBehavior,
        modifier = modifier
    ) {
        NavBarItem(
            screen = Screen.List,
            onClick = {
                if (currentScreen != Screen.List) {
                    backStack.clear()
                    backStack.add(Screen.List)
                }
            },
            icon = {
                Icon(
                    if (it) Icons.Default.Home else Icons.Outlined.Home,
                    null
                )
            },
            label = stringResource(Res.string.home),
        )

        NavBarItem(
            screen = Screen.Favorites,
            onClick = {
                if (currentScreen != Screen.Favorites) {
                    backStack.removeAll { it == Screen.Favorites }
                    backStack.add(Screen.Favorites)
                }
            },
            icon = {
                Icon(
                    if (it) Icons.Default.Favorite else Icons.Outlined.FavoriteBorder,
                    null
                )
            },
            label = stringResource(Res.string.favorites),
        )

        NavBarItem(
            screen = Screen.CustomList,
            onClick = {
                if (currentScreen != Screen.CustomList) {
                    backStack.removeAll { it == Screen.CustomList }
                    backStack.add(Screen.CustomList)
                }
            },
            icon = {
                Icon(
                    if (it) Icons.AutoMirrored.Filled.List else Icons.AutoMirrored.Outlined.List,
                    null
                )
            },
            label = stringResource(Res.string.lists),
        )

        NavBarItem(
            screen = Screen.Settings,
            onClick = {
                if (currentScreen != Screen.Settings) {
                    backStack.removeAll { it == Screen.Settings }
                    backStack.add(Screen.Settings)
                }
            },
            icon = {
                Icon(
                    if (it) Icons.Default.Settings else Icons.Outlined.Settings,
                    null
                )
            },
            label = stringResource(Res.string.settings),
        )
    }
}

@Composable
fun CivitRail(
    modifier: Modifier = Modifier,
) {
    val backStack = koinInject<NavigationHandler>().backStack
    val currentScreen = backStack.lastOrNull()

    @Composable
    fun NavRailItem(
        screen: NavKey,
        onClick: () -> Unit,
        icon: @Composable (Boolean) -> Unit,
        label: String,
    ) {
        NavigationRailItem(
            selected = screen == currentScreen,
            onClick = onClick,
            icon = { icon(screen == currentScreen) },
            label = { Text(label) },
        )
    }

    NavigationRail(
        modifier = modifier,
        header = {

        }
    ) {
        NavRailItem(
            screen = Screen.List,
            onClick = {
                if (currentScreen != Screen.List) {
                    backStack.clear()
                    backStack.add(Screen.List)
                }
            },
            icon = {
                Icon(
                    if (it) Icons.Default.Home else Icons.Outlined.Home,
                    null
                )
            },
            label = stringResource(Res.string.home),
        )

        NavRailItem(
            screen = Screen.Favorites,
            onClick = {
                if (currentScreen != Screen.Favorites) {
                    backStack.removeAll { it == Screen.Favorites }
                    backStack.add(Screen.Favorites)
                }
            },
            icon = {
                Icon(
                    if (it) Icons.Default.Favorite else Icons.Outlined.FavoriteBorder,
                    null
                )
            },
            label = stringResource(Res.string.favorites),
        )

        NavRailItem(
            screen = Screen.CustomList,
            onClick = {
                if (currentScreen != Screen.CustomList) {
                    backStack.removeAll { it == Screen.CustomList }
                    backStack.add(Screen.CustomList)
                }
            },
            icon = {
                Icon(
                    if (it) Icons.AutoMirrored.Filled.List else Icons.AutoMirrored.Outlined.List,
                    null
                )
            },
            label = stringResource(Res.string.lists),
        )

        NavRailItem(
            screen = Screen.Search,
            onClick = {
                if (currentScreen != Screen.Search) {
                    backStack.removeAll { it == Screen.Search }
                    backStack.add(Screen.Search)
                }
            },
            icon = {
                Icon(
                    if (it) Icons.Default.Search else Icons.Outlined.Search,
                    null
                )
            },
            label = stringResource(Res.string.search),
        )

        NavRailItem(
            screen = Screen.Settings,
            onClick = {
                if (currentScreen != Screen.Settings) {
                    backStack.removeAll { it == Screen.Settings }
                    backStack.add(Screen.Settings)
                }
            },
            icon = {
                Icon(
                    if (it) Icons.Default.Settings else Icons.Outlined.Settings,
                    null
                )
            },
            label = stringResource(Res.string.settings),
        )
    }
}