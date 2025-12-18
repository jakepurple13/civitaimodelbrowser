package com.programmersbox.common.settings

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.NoAdultContent
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.ExperimentalMaterial3Api
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
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.programmersbox.common.BackButton
import com.programmersbox.common.ComposableUtils
import com.programmersbox.common.DataStore
import com.programmersbox.resources.Res
import com.programmersbox.resources.civitai_logo
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.painterResource
import org.koin.compose.koinInject
import kotlin.math.roundToInt

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NsfwSettingsScreen() {
    val dataStore = koinInject<DataStore>()

    val scrollBehavior = TopAppBarDefaults.enterAlwaysScrollBehavior()

    Scaffold(
        topBar = {
            LargeTopAppBar(
                title = { Text("NSFW Settings") },
                navigationIcon = { BackButton() },
                scrollBehavior = scrollBehavior
            )
        },
        modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection)
    ) { padding ->
        NsfwSettings(
            dataStore = dataStore,
            modifier = Modifier.padding(padding)
        )
    }
}

@Composable
private fun NsfwSettings(
    dataStore: DataStore,
    modifier: Modifier = Modifier,
) {
    var showNsfw by dataStore.showNsfw()
    val includeNsfw = remember { dataStore.includeNsfw }
    val scope = rememberCoroutineScope()

    val includeNsfwEnabled by includeNsfw.flow.collectAsStateWithLifecycle(false)

    Column(
        modifier = modifier.verticalScroll(rememberScrollState())
    ) {
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
                        checked = showNsfw,
                        onCheckedChange = { showNsfw = it }
                    )
                },
                leadingContent = {
                    Icon(
                        if (showNsfw)
                            Icons.Default.Visibility
                        else
                            Icons.Default.VisibilityOff,
                        null
                    )
                }
            )
        }

        AnimatedVisibility(!showNsfw && includeNsfwEnabled) {
            Column(
                modifier = Modifier.fillMaxWidth()
            ) {
                var nsfwBlurStrength by dataStore.hideNsfwStrength()
                ListItem(
                    overlineContent = { Text("Default is 6") },
                    headlineContent = { Text("Strength: ${nsfwBlurStrength.roundToInt()}") },
                    supportingContent = {
                        val range = 5f..100f
                        Slider(
                            value = nsfwBlurStrength,
                            onValueChange = { nsfwBlurStrength = it },
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
}