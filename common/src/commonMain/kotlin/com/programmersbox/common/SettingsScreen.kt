package com.programmersbox.common

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen() {
    val showNsfw = remember { DataStore.showNsfw }
    val hideNsfwStrength = remember { DataStore.hideNsfwStrength }
    val navController = LocalNavController.current
    val scrollBehavior = TopAppBarDefaults.enterAlwaysScrollBehavior()
    val scope = rememberCoroutineScope()

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
        }
    ) { padding ->
        Column(
            modifier = Modifier.padding(padding)
        ) {
            val isNsfwEnabled by showNsfw.flow.collectAsState(false)
            ListItem(
                headlineContent = { Text("Show NSFW Content?") },
                trailingContent = {
                    Switch(
                        checked = isNsfwEnabled,
                        onCheckedChange = { scope.launch { showNsfw.update(it) } }
                    )
                },
                supportingContent = {
                    AnimatedVisibility(!isNsfwEnabled) {
                        Column {
                            val nsfwBlurStrength by hideNsfwStrength.flow.collectAsState(6f)
                            Text("Strength: ${nsfwBlurStrength.roundToInt()}")
                            Slider(
                                value = nsfwBlurStrength,
                                onValueChange = { scope.launch { hideNsfwStrength.update(it) } },
                                steps = 5,
                                valueRange = 5f..10f
                            )
                        }
                    }
                }
            )
        }
    }
}