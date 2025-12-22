package com.programmersbox.common.settings

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.BlurOff
import androidx.compose.material.icons.filled.BlurOn
import androidx.compose.material.icons.filled.BorderBottom
import androidx.compose.material.icons.filled.Brightness4
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.LargeTopAppBar
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.nestedscroll.nestedScroll
import com.programmersbox.common.BackButton
import com.programmersbox.common.DataStore
import com.programmersbox.common.ThemeMode
import org.koin.compose.koinInject

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BehaviorSettingsScreen() {
    val dataStore = koinInject<DataStore>()

    val scrollBehavior = TopAppBarDefaults.enterAlwaysScrollBehavior()

    Scaffold(
        topBar = {
            LargeTopAppBar(
                title = { Text("Behavior Settings") },
                navigationIcon = { BackButton() },
                scrollBehavior = scrollBehavior
            )
        },
        modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection)
    ) { padding ->
        BehaviorSettings(
            dataStore = dataStore,
            modifier = Modifier.padding(padding)
        )
    }
}

@Composable
private fun BehaviorSettings(
    dataStore: DataStore,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier.verticalScroll(rememberScrollState())
    ) {
        var useToolbar by dataStore.rememberUseToolbar()
        var showBlur by dataStore.rememberShowBlur()
        Card(
            onClick = { showBlur = !showBlur }
        ) {
            ListItem(
                leadingContent = {
                    Icon(
                        if (showBlur) Icons.Default.BlurOn else Icons.Default.BlurOff,
                        null
                    )
                },
                headlineContent = { Text("Show Blur") },
                trailingContent = {
                    Switch(
                        checked = showBlur,
                        onCheckedChange = { showBlur = it }
                    )
                }
            )
        }

        Card(
            onClick = { useToolbar = !useToolbar }
        ) {
            ListItem(
                leadingContent = { Icon(Icons.Default.BorderBottom, null) },
                headlineContent = { Text("Use Toolbar") },
                trailingContent = {
                    Switch(
                        checked = useToolbar,
                        onCheckedChange = { useToolbar = it }
                    )
                }
            )
        }

        var themeMode by dataStore.rememberThemeMode()

        var showThemeModeDialog by remember { mutableStateOf(false) }

        if (showThemeModeDialog) {
            AlertDialog(
                onDismissRequest = { showThemeModeDialog = false },
                title = { Text("Theme Mode") },
                text = {
                    Column {
                        ThemeMode.entries.forEach {
                            Card(
                                onClick = { themeMode = it },
                                colors = CardDefaults.cardColors(
                                    containerColor = Color.Transparent
                                )
                            ) {
                                ListItem(
                                    headlineContent = { Text(it.name) },
                                    trailingContent = {
                                        RadioButton(
                                            selected = it == themeMode,
                                            onClick = null
                                        )
                                    },
                                    colors = ListItemDefaults.colors(
                                        containerColor = Color.Transparent
                                    )
                                )
                            }
                        }
                    }
                },
                confirmButton = {
                    TextButton(
                        onClick = { showThemeModeDialog = false }
                    ) { Text("Confirm") }
                }
            )
        }

        Card(
            onClick = { showThemeModeDialog = true }
        ) {
            ListItem(
                leadingContent = { Icon(Icons.Default.Brightness4, null) },
                headlineContent = { Text("Theme Mode") },
                supportingContent = { Text(themeMode.name) },
                trailingContent = {
                    Icon(
                        Icons.Default.ArrowDropDown,
                        null
                    )
                }
            )
        }

        /*HorizontalDivider()

        var middleNavigation by dataStore.rememberMiddleNavigation()
        var showMiddleNavigationDialog by remember { mutableStateOf(false) }

        if (showMiddleNavigationDialog) {
            AlertDialog(
                onDismissRequest = { showMiddleNavigationDialog = false },
                title = { Text("Middle Navigation") },
                text = {
                    Column {
                        MiddleNavigation.entries.forEach {
                            Card(
                                onClick = { middleNavigation = it },
                                colors = CardDefaults.cardColors(
                                    containerColor = Color.Transparent
                                )
                            ) {
                                ListItem(
                                    headlineContent = { Text(it.name) },
                                    trailingContent = {
                                        RadioButton(
                                            selected = it == middleNavigation,
                                            onClick = null
                                        )
                                    },
                                    colors = ListItemDefaults.colors(
                                        containerColor = Color.Transparent
                                    )
                                )
                            }
                        }
                    }
                },
                confirmButton = {
                    TextButton(
                        onClick = { showMiddleNavigationDialog = false }
                    ) { Text("Confirm") }
                }
            )
        }

        Card(
            onClick = { showMiddleNavigationDialog = true }
        ) {
            ListItem(
                leadingContent = { Icon(Icons.Default.Navigation, null) },
                headlineContent = { Text("Middle Navigation") },
                supportingContent = { Text(middleNavigation.name) },
                trailingContent = {
                    Icon(
                        Icons.Default.ArrowDropDown,
                        null
                    )
                }
            )
        }*/
    }
}