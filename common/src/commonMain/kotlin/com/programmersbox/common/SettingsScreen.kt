package com.programmersbox.common

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import kotlinx.coroutines.launch
import moe.tlaster.precompose.flow.collectAsStateWithLifecycle
import kotlin.math.roundToInt

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen() {
    val navController = LocalNavController.current
    val scrollBehavior = TopAppBarDefaults.enterAlwaysScrollBehavior()
    val scope = rememberCoroutineScope()

    val dataStore = LocalDataStore.current
    val showNsfw = remember { dataStore.showNsfw }
    val hideNsfwStrength = remember { dataStore.hideNsfwStrength }
    val includeNsfw = remember { dataStore.includeNsfw }

    Scaffold(
        topBar = {
            LargeTopAppBar(
                title = { Text("Settings") },
                navigationIcon = {
                    IconButton(
                        onClick = { navController.popBackStack() }
                    ) { Icon(Icons.Default.ArrowBack, null) }
                },
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
                val nsfwBlurStrength by hideNsfwStrength.flow.collectAsStateWithLifecycle(6f)
                ListItem(
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
            }
            Divider()
            //https://github.com/civitai/civitai/wiki/REST-API-Reference
        }
    }
}