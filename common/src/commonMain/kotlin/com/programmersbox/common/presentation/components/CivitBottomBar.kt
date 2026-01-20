package com.programmersbox.common.presentation.components

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
import com.programmersbox.common.Screen
import com.programmersbox.common.di.NavigationHandler
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

    BottomAppBar(
        containerColor = if (showBlur) Color.Transparent else BottomAppBarDefaults.containerColor,
        scrollBehavior = bottomBarScrollBehavior,
        modifier = modifier
    ) {
        NavigationBarItem(
            selected = Screen.List == currentScreen,
            onClick = {
                if (currentScreen != Screen.List) {
                    backStack.clear()
                    backStack.add(Screen.List)
                }
            },
            icon = {
                Icon(
                    if (Screen.List == currentScreen) Icons.Default.Home
                    else Icons.Outlined.Home, null
                )
            },
            label = { Text("Home") },
        )

        NavigationBarItem(
            selected = Screen.Favorites == currentScreen,
            onClick = {
                if (currentScreen != Screen.Favorites) {
                    backStack.removeAll { it == Screen.Favorites }
                    backStack.add(Screen.Favorites)
                }
            },
            icon = {
                Icon(
                    if (Screen.Favorites == currentScreen) Icons.Default.Favorite
                    else Icons.Outlined.FavoriteBorder, null
                )
            },
            label = { Text("Favorites") },
        )

        NavigationBarItem(
            selected = Screen.CustomList == currentScreen,
            onClick = {
                if (currentScreen != Screen.CustomList) {
                    backStack.removeAll { it == Screen.CustomList }
                    backStack.add(Screen.CustomList)
                }
            },
            icon = {
                Icon(
                    if (Screen.CustomList == currentScreen) Icons.AutoMirrored.Filled.List
                    else Icons.AutoMirrored.Outlined.List, null
                )
            },
            label = { Text("Lists") },
        )

        NavigationBarItem(
            selected = Screen.Settings == currentScreen,
            onClick = {
                if (currentScreen != Screen.Settings) {
                    backStack.removeAll { it == Screen.Settings }
                    backStack.add(Screen.Settings)
                }
            },
            icon = {
                Icon(
                    if (Screen.Settings == currentScreen) Icons.Default.Settings
                    else Icons.Outlined.Settings, null
                )
            },
            label = { Text("Settings") },
        )
    }
}

@Composable
fun CivitRail(
    modifier: Modifier = Modifier,
) {
    val backStack = koinInject<NavigationHandler>().backStack
    val currentScreen = backStack.lastOrNull()

    NavigationRail(
        modifier = modifier,
        header = {

        }
    ) {
        NavigationRailItem(
            selected = Screen.List == currentScreen,
            onClick = {
                if (currentScreen != Screen.List) {
                    backStack.clear()
                    backStack.add(Screen.List)
                }
            },
            icon = {
                Icon(
                    if (Screen.List == currentScreen) Icons.Default.Home
                    else Icons.Outlined.Home,
                    null
                )
            },
            label = { Text("Home") },
        )

        NavigationRailItem(
            selected = Screen.Favorites == currentScreen,
            onClick = {
                if (currentScreen != Screen.Favorites) {
                    backStack.removeAll { it == Screen.Favorites }
                    backStack.add(Screen.Favorites)
                }
            },
            icon = {
                Icon(
                    if (Screen.Favorites == currentScreen) Icons.Default.Favorite
                    else Icons.Outlined.FavoriteBorder,
                    null
                )
            },
            label = { Text("Favorites") },
        )

        NavigationRailItem(
            selected = Screen.CustomList == currentScreen,
            onClick = {
                if (currentScreen != Screen.CustomList) {
                    backStack.removeAll { it == Screen.CustomList }
                    backStack.add(Screen.CustomList)
                }
            },
            icon = {
                Icon(
                    if (Screen.CustomList == currentScreen) Icons.AutoMirrored.Filled.List
                    else Icons.AutoMirrored.Outlined.List,
                    null
                )
            },
            label = { Text("Lists") },
        )

        NavigationRailItem(
            selected = Screen.Search == currentScreen,
            onClick = {
                if (currentScreen != Screen.Search) {
                    backStack.removeAll { it == Screen.Search }
                    backStack.add(Screen.Search)
                }
            },
            icon = {
                Icon(
                    if (Screen.Search == currentScreen) Icons.Default.Search
                    else Icons.Outlined.Search,
                    null
                )
            },
            label = { Text("Search") },
        )

        NavigationRailItem(
            selected = Screen.Settings == currentScreen,
            onClick = {
                if (currentScreen != Screen.Settings) {
                    backStack.removeAll { it == Screen.Settings }
                    backStack.add(Screen.Settings)
                }
            },
            icon = {
                Icon(
                    if (Screen.Settings == currentScreen) Icons.Default.Settings
                    else Icons.Outlined.Settings,
                    null
                )
            },
            label = { Text("Settings") },
        )
    }
}