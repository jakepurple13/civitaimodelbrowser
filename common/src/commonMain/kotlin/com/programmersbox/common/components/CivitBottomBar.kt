package com.programmersbox.common.components

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CivitBottomBar(
    showBlur: Boolean,
    isHome: Boolean,
    isSettings: Boolean,
    isLists: Boolean,
    bottomBarScrollBehavior: BottomAppBarScrollBehavior?,
    onNavigateToHome: () -> Unit,
    onNavigateToSettings: () -> Unit,
    onNavigateToLists: () -> Unit,
    modifier: Modifier = Modifier,
) {
    BottomAppBar(
        containerColor = if (showBlur) Color.Transparent else BottomAppBarDefaults.containerColor,
        scrollBehavior = bottomBarScrollBehavior,
        modifier = modifier
    ) {
        NavigationBarItem(
            selected = isHome,
            onClick = onNavigateToHome,
            icon = { Icon(Icons.Default.Home, null) },
            label = { Text("Home") },
        )
        NavigationBarItem(
            selected = isLists,
            onClick = onNavigateToLists,
            icon = { Icon(Icons.AutoMirrored.Filled.List, null) },
            label = { Text("Lists") },
        )
        NavigationBarItem(
            selected = isSettings,
            onClick = onNavigateToSettings,
            icon = { Icon(Icons.Default.Settings, null) },
            label = { Text("Settings") },
        )
    }
}