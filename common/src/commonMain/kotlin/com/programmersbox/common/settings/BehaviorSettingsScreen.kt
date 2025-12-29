package com.programmersbox.common.settings

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.BlurCircular
import androidx.compose.material.icons.filled.BlurOff
import androidx.compose.material.icons.filled.BlurOn
import androidx.compose.material.icons.filled.BorderBottom
import androidx.compose.material.icons.filled.Brightness4
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.LargeTopAppBar
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import com.programmersbox.common.BackButton
import com.programmersbox.common.BlurType
import com.programmersbox.common.DataStore
import com.programmersbox.common.HazeBlur
import com.programmersbox.common.ThemeMode
import com.programmersbox.resources.Res
import com.programmersbox.resources.civitai_logo
import dev.chrisbanes.haze.HazeState
import dev.chrisbanes.haze.HazeStyle
import dev.chrisbanes.haze.hazeEffect
import dev.chrisbanes.haze.hazeSource
import dev.chrisbanes.haze.rememberHazeState
import org.jetbrains.compose.resources.painterResource
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
        var blurType by dataStore.rememberBlurType()
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

        AnimatedVisibility(showBlur) {
            var showBlurOptions by remember { mutableStateOf(false) }

            Column {
                Card(
                    onClick = { showBlurOptions = !showBlurOptions }
                ) {
                    ListItem(
                        headlineContent = { Text("Blur Type") },
                        trailingContent = { Text("${blurType.type.name} ${blurType.level.name}") },
                        leadingContent = { Icon(Icons.Default.BlurCircular, null) }
                    )

                    AnimatedVisibility(showBlurOptions) {
                        Column {
                            HazeBlur.entries.forEachIndexed { index, blur ->
                                FlowRow(
                                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                                    verticalArrangement = Arrangement.spacedBy(16.dp),
                                    modifier = Modifier.padding(16.dp)
                                ) {
                                    blur.levels.forEach { level ->
                                        Surface(
                                            onClick = { blurType = BlurType(blur, level) },
                                            shape = MaterialTheme.shapes.large
                                        ) {
                                            Box {
                                                val hazeState = rememberHazeState()
                                                Image(
                                                    painter = painterResource(Res.drawable.civitai_logo),
                                                    contentDescription = null,
                                                    contentScale = ContentScale.FillBounds,
                                                    modifier = Modifier
                                                        .matchParentSize()
                                                        .hazeSource(hazeState)
                                                )
                                                MaterialsCard(
                                                    name = "$blur $level",
                                                    state = hazeState,
                                                    shape = MaterialTheme.shapes.large,
                                                    style = blur.toHazeStyle(level)
                                                )

                                                if (blurType == BlurType(blur, level)) {
                                                    Icon(
                                                        Icons.Default.Check,
                                                        null,
                                                        modifier = Modifier
                                                            .align(Alignment.Center)
                                                            .padding(8.dp)
                                                    )
                                                }
                                            }
                                        }
                                    }
                                }
                                if (index < HazeBlur.entries.size - 1) HorizontalDivider()
                            }
                        }
                    }
                }
                AnimatedVisibility(!showBlurOptions) {
                    HorizontalDivider()
                }
            }
        }

        AnimatedVisibility(!showBlur) {
            HorizontalDivider()
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

@Composable
private fun MaterialsCard(
    name: String,
    state: HazeState,
    style: HazeStyle,
    modifier: Modifier = Modifier,
    shape: Shape = MaterialTheme.shapes.medium,
) {
    Card(
        shape = shape,
        colors = CardDefaults.cardColors(
            containerColor = Color.Transparent,
            contentColor = MaterialTheme.colorScheme.onSurface,
        ),
        modifier = modifier.size(160.dp),
    ) {
        Box(
            Modifier
                .fillMaxSize()
                .hazeEffect(state = state) {
                    this.style = style
                }
                .padding(16.dp),
        ) {
            Text(name)
        }
    }
}