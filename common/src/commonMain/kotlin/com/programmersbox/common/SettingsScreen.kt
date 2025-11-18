package com.programmersbox.common

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
import androidx.compose.material.icons.filled.BlurOff
import androidx.compose.material.icons.filled.BlurOn
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.LargeTopAppBar
import androidx.compose.material3.ListItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.programmersbox.common.components.icons.Github
import com.programmersbox.common.db.CivitDb
import com.programmersbox.common.db.FavoritesDao
import com.programmersbox.resources.Res
import com.programmersbox.resources.civitai_logo
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.painterResource
import org.koin.compose.koinInject
import kotlin.math.roundToInt

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    onExport: (CivitDb) -> Unit = {},
    onImport: () -> String = { "" },
    export: @Composable () -> Unit = {},
    import: (@Composable () -> Unit)? = null,
    onNavigateToBlacklisted: () -> Unit,
) {
    val scrollBehavior = TopAppBarDefaults.enterAlwaysScrollBehavior()
    val scope = rememberCoroutineScope()

    val dataStore = koinInject<DataStore>()
    val showNsfw = remember { dataStore.showNsfw }
    val hideNsfwStrength = remember { dataStore.hideNsfwStrength }
    val includeNsfw = remember { dataStore.includeNsfw }
    val dao = koinInject<FavoritesDao>()
    var showBlur by dataStore.rememberShowBlur()

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
            val isNsfwEnabled by showNsfw.flow.collectAsStateWithLifecycle(false)
            val includeNsfwEnabled by includeNsfw.flow.collectAsStateWithLifecycle(false)

            ListItem(
                headlineContent = { Text("Include NSFW Content?") },
                trailingContent = {
                    Switch(
                        checked = includeNsfwEnabled,
                        onCheckedChange = { scope.launch { includeNsfw.update(it) } }
                    )
                }
            )

            AnimatedVisibility(includeNsfwEnabled) {
                ListItem(
                    headlineContent = { Text("Show NSFW Content?") },
                    trailingContent = {
                        Switch(
                            checked = isNsfwEnabled,
                            onCheckedChange = { scope.launch { showNsfw.update(it) } }
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

            HorizontalDivider()

            Card(
                onClick = onNavigateToBlacklisted
            ) {
                ListItem(
                    headlineContent = { Text("View Blacklisted Models") }
                )
            }

            HorizontalDivider()

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

            HorizontalDivider()

            export()

            Card(
                onClick = {
                    scope.launch { onExport(dao.export()) }
                }
            ) {
                ListItem(
                    headlineContent = { Text("Export Favorites") }
                )
            }

            import?.invoke() ?: Card(
                onClick = { onImport() }
            ) {
                ListItem(
                    headlineContent = { Text("Import Favorites") }
                )
            }

            ExtraSettings()

            HorizontalDivider()

            val uriHandler = LocalUriHandler.current

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
                                .size(48.dp)
                                .clip(CircleShape)
                        )
                    }
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
                                .size(48.dp)
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
        }
    }
}

@Composable
expect fun ColumnScope.ExtraSettings()