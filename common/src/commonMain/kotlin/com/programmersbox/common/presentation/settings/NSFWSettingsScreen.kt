package com.programmersbox.common.presentation.settings

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
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
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.LargeTopAppBar
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.windowsizeclass.WindowWidthSizeClass
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
import com.programmersbox.common.LocalWindowClassSize
import com.programmersbox.resources.Res
import com.programmersbox.resources.civitai_logo
import com.programmersbox.resources.default_is_6
import com.programmersbox.resources.include_nsfw_content
import com.programmersbox.resources.nsfw_settings
import com.programmersbox.resources.show_nsfw_content
import com.programmersbox.resources.strength
import com.sinasamaki.chroma.dial.Dial
import com.sinasamaki.chroma.dial.DialColors
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
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
                title = { Text(stringResource(Res.string.nsfw_settings)) },
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

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun NsfwSettings(
    dataStore: DataStore,
    modifier: Modifier = Modifier,
) {
    var showNsfw by dataStore.showNsfw()
    val includeNsfw = remember { dataStore.includeNsfw }
    val scope = rememberCoroutineScope()

    val includeNsfwEnabled by includeNsfw.flow.collectAsStateWithLifecycle(false)

    Column(
        verticalArrangement = Arrangement.spacedBy(ListItemDefaults.SegmentedGap),
        modifier = modifier.verticalScroll(rememberScrollState())
    ) {
        ListItem(
            content = { Text(stringResource(Res.string.include_nsfw_content)) },
            trailingContent = {
                Switch(
                    checked = includeNsfwEnabled,
                    onCheckedChange = null
                )
            },
            checked = includeNsfwEnabled,
            onCheckedChange = { scope.launch { includeNsfw.update(it) } },
            leadingContent = { Icon(Icons.Default.NoAdultContent, null) },
            colors = ListItemDefaults.colors(containerColor = MaterialTheme.colorScheme.surfaceContainer)
        )

        AnimatedVisibility(includeNsfwEnabled) {
            ListItem(
                content = { Text(stringResource(Res.string.show_nsfw_content)) },
                trailingContent = {
                    Switch(
                        checked = showNsfw,
                        onCheckedChange = null
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
                },
                checked = showNsfw,
                onCheckedChange = { showNsfw = it },
                colors = ListItemDefaults.colors(containerColor = MaterialTheme.colorScheme.surfaceContainer)
            )
        }

        AnimatedVisibility(!showNsfw && includeNsfwEnabled) {
            var nsfwBlurStrength by dataStore.hideNsfwStrength()
            when (LocalWindowClassSize.current) {
                WindowWidthSizeClass.Compact -> {
                    Column(
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        ListItem(
                            overlineContent = { Text(stringResource(Res.string.default_is_6)) },
                            headlineContent = {
                                Text(
                                    stringResource(
                                        Res.string.strength,
                                        nsfwBlurStrength.roundToInt()
                                    )
                                )
                            },
                            supportingContent = {
                                val range = 5f..100f
                                Slider(
                                    value = nsfwBlurStrength,
                                    onValueChange = { nsfwBlurStrength = it },
                                    steps = (range.endInclusive - range.start).roundToInt(),
                                    valueRange = range
                                )
                            },
                            colors = ListItemDefaults.colors(containerColor = MaterialTheme.colorScheme.surfaceContainer)
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

                else -> {
                    Column {
                        ListItem(
                            headlineContent = { Text("Blur Strength") },
                            overlineContent = { Text(stringResource(Res.string.default_is_6)) },
                        )

                        Row(
                            horizontalArrangement = Arrangement.SpaceEvenly,
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Image(
                                painter = painterResource(Res.drawable.civitai_logo),
                                contentDescription = null,
                                modifier = Modifier
                                    .size(
                                        ComposableUtils.IMAGE_WIDTH,
                                        ComposableUtils.IMAGE_HEIGHT
                                    )
                                    .blur(nsfwBlurStrength.dp)
                            )

                            Box(
                                contentAlignment = Alignment.Center
                            ) {
                                val animatedDegree by animateFloatAsState(
                                    targetValue = nsfwBlurStrength,
                                    animationSpec = spring(
                                        stiffness = Spring.StiffnessHigh,
                                        dampingRatio = Spring.DampingRatioLowBouncy,
                                    )
                                )

                                Dial(
                                    degree = animatedDegree,
                                    onDegreeChanged = { nsfwBlurStrength = it },
                                    startDegrees = 0f,
                                    sweepDegrees = 360f,
                                    colors = DialColors.default(
                                        activeTrackColor = MaterialTheme.colorScheme.primaryContainer,
                                        inactiveTrackColor = MaterialTheme.colorScheme.surfaceContainer,
                                        thumbColor = MaterialTheme.colorScheme.primary,
                                        thumbStrokeColor = MaterialTheme.colorScheme.primary,
                                    ),
                                    modifier = Modifier.size(150.dp)
                                )

                                Text(
                                    nsfwBlurStrength.roundToInt().toString(),
                                    modifier = Modifier.padding(8.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}