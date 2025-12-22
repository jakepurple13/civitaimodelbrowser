package com.programmersbox.common.components

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.BottomAppBar
import androidx.compose.material3.BottomAppBarDefaults
import androidx.compose.material3.BottomAppBarScrollBehavior
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBarItem
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
            icon = { Icon(Icons.Default.Home, null) },
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
            icon = { Icon(Icons.Default.Favorite, null) },
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
            icon = { Icon(Icons.AutoMirrored.Filled.List, null) },
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
            icon = { Icon(Icons.Default.Settings, null) },
            label = { Text("Settings") },
        )
    }
}