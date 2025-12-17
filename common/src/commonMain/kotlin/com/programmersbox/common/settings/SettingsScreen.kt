package com.programmersbox.common.settings

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Backup
import androidx.compose.material.icons.filled.BlurOff
import androidx.compose.material.icons.filled.BlurOn
import androidx.compose.material.icons.filled.BorderBottom
import androidx.compose.material.icons.filled.Brightness4
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.NoAdultContent
import androidx.compose.material.icons.filled.QueryStats
import androidx.compose.material.icons.filled.Restore
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.LargeTopAppBar
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.programmersbox.common.ApplicationInfo
import com.programmersbox.common.BackButton
import com.programmersbox.common.ComposableUtils
import com.programmersbox.common.DataStore
import com.programmersbox.common.ThemeMode
import com.programmersbox.common.components.icons.Github
import com.programmersbox.common.getPlatformName
import com.programmersbox.resources.Res
import com.programmersbox.resources.civitai_logo
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.painterResource
import org.koin.compose.koinInject
import kotlin.math.roundToInt

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    onNavigateToQrCode: () -> Unit,
    onNavigateToBackup: () -> Unit,
    onNavigateToRestore: () -> Unit,
    onNavigateToStats: () -> Unit,
    onNavigateToAbout: () -> Unit,
) {
    val scrollBehavior = TopAppBarDefaults.enterAlwaysScrollBehavior()

    val dataStore = koinInject<DataStore>()

    Scaffold(
        topBar = {
            LargeTopAppBar(
                title = { Text("Settings") },
                navigationIcon = { BackButton() },
                scrollBehavior = scrollBehavior
            )
        },
        modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection)
    ) { padding ->
        Column(
            modifier = Modifier
                .verticalScroll(rememberScrollState())
                .fillMaxSize()
                .padding(padding)
        ) {
            Card(
                onClick = onNavigateToQrCode
            ) {
                ListItem(
                    headlineContent = { Text("Scan QR Code") },
                )
            }

            HorizontalDivider()

            NsfwSettings(dataStore)

            HorizontalDivider()

            BehaviorSettings(dataStore)

            HorizontalDivider()

            BackupRestoreSettings(
                onNavigateToBackup = onNavigateToBackup,
                onNavigateToRestore = onNavigateToRestore,
            )

            ExtraSettings()

            HorizontalDivider()

            Card(
                onClick = onNavigateToStats
            ) {
                ListItem(
                    headlineContent = { Text("Stats") },
                    leadingContent = { Icon(Icons.Default.QueryStats, null) }
                )
            }

            HorizontalDivider()

            AboutSettings(
                onNavigateToAbout = onNavigateToAbout,
            )
        }
    }
}

@Composable
private fun NsfwSettings(
    dataStore: DataStore,
) {
    val showNsfw = remember { dataStore.showNsfw }
    val hideNsfwStrength = remember { dataStore.hideNsfwStrength }
    val includeNsfw = remember { dataStore.includeNsfw }
    val scope = rememberCoroutineScope()

    val isNsfwEnabled by showNsfw.flow.collectAsStateWithLifecycle(false)
    val includeNsfwEnabled by includeNsfw.flow.collectAsStateWithLifecycle(false)

    ListItem(
        headlineContent = { Text("Include NSFW Content?") },
        trailingContent = {
            Switch(
                checked = includeNsfwEnabled,
                onCheckedChange = { scope.launch { includeNsfw.update(it) } }
            )
        },
        leadingContent = { Icon(Icons.Default.NoAdultContent, null) }
    )

    AnimatedVisibility(includeNsfwEnabled) {
        ListItem(
            headlineContent = { Text("Show NSFW Content?") },
            trailingContent = {
                Switch(
                    checked = isNsfwEnabled,
                    onCheckedChange = { scope.launch { showNsfw.update(it) } }
                )
            },
            leadingContent = {
                Icon(
                    if (isNsfwEnabled)
                        Icons.Default.Visibility
                    else
                        Icons.Default.VisibilityOff,
                    null
                )
            }
        )
    }

    AnimatedVisibility(!isNsfwEnabled && includeNsfwEnabled) {
        Column(
            modifier = Modifier.fillMaxWidth()
        ) {
            val nsfwBlurStrength by hideNsfwStrength.flow.collectAsStateWithLifecycle(6f)
            ListItem(
                overlineContent = { Text("Default is 6") },
                headlineContent = { Text("Strength: ${nsfwBlurStrength.roundToInt()}") },
                supportingContent = {
                    val range = 5f..100f
                    Slider(
                        value = nsfwBlurStrength,
                        onValueChange = { scope.launch { hideNsfwStrength.update(it) } },
                        steps = (range.endInclusive - range.start).roundToInt(),
                        valueRange = range
                    )
                }
            )

            Image(
                painter = painterResource(Res.drawable.civitai_logo),
                contentDescription = null,
                modifier = Modifier
                    .align(Alignment.CenterHorizontally)
                    .size(
                        ComposableUtils.IMAGE_WIDTH,
                        ComposableUtils.IMAGE_HEIGHT
                    )
                    .blur(nsfwBlurStrength.dp)
            )
        }
    }
}

@Composable
private fun BehaviorSettings(dataStore: DataStore) {
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
}

@Composable
private fun BackupRestoreSettings(
    onNavigateToBackup: () -> Unit,
    onNavigateToRestore: () -> Unit,
) {
    Card(
        onClick = onNavigateToBackup
    ) {
        ListItem(
            headlineContent = { Text("Backup") },
            leadingContent = { Icon(Icons.Default.Backup, null) }
        )
    }

    Card(
        onClick = onNavigateToRestore
    ) {
        ListItem(
            headlineContent = { Text("Restore") },
            leadingContent = { Icon(Icons.Default.Restore, null) }
        )
    }
}

@Composable
private fun ColumnScope.AboutSettings(
    onNavigateToAbout: () -> Unit,
) {
    val uriHandler = LocalUriHandler.current

    Card(
        onClick = onNavigateToAbout
    ) {
        ListItem(
            headlineContent = { Text("About") },
            leadingContent = { Icon(Icons.Default.Info, null) }
        )
    }

    Card(
        onClick = { uriHandler.openUri("https://civitai.com/") }
    ) {
        ListItem(
            headlineContent = { Text("Open CivitAi") },
            leadingContent = {
                Image(
                    painter = painterResource(Res.drawable.civitai_logo),
                    contentDescription = null,
                    modifier = Modifier
                        .size(24.dp)
                        .clip(CircleShape)
                )
            }
        )
    }

    Card(
        onClick = { uriHandler.openUri("https://github.com/civitai/civitai/wiki/REST-API-Reference") }
    ) {
        ListItem(
            headlineContent = { Text("Open CivitAi REST API") },
            leadingContent = {
                Icon(
                    Icons.Github,
                    contentDescription = null,
                    modifier = Modifier
                        .size(24.dp)
                        .clip(CircleShape)
                )
            }
        )
    }

    Card(
        onClick = { uriHandler.openUri("https://github.com/jakepurple13/civitaimodelbrowser/") }
    ) {
        ListItem(
            headlineContent = { Text("Open CivitAi Model Browser GitHub") },
            leadingContent = {
                Icon(
                    Icons.Github,
                    contentDescription = null,
                    modifier = Modifier
                        .size(24.dp)
                        .clip(CircleShape)
                )
            }
        )
    }

    HorizontalDivider()

    Text(
        remember { getPlatformName() },
        modifier = Modifier.align(Alignment.CenterHorizontally)
    )

    Text(
        koinInject<ApplicationInfo>().versionName,
        modifier = Modifier.align(Alignment.CenterHorizontally)
    )
}

@Composable
expect fun ColumnScope.ExtraSettings()